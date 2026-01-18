<?php
require_once 'db_connect.php';

header('Content-Type: application/json');

// Check if this is a POST request
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(array("status" => "error", "message" => "Only POST method allowed"));
    exit();
}

// Get user_id from POST data
$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';

if (empty($user_id)) {
    echo json_encode(array("status" => "error", "message" => "User ID is required"));
    exit();
}

try {
    // Fetch all history for this user, ordered by most recent first
    $stmt = $conn->prepare("SELECT id, user_id, image_path, prediction_result, confidence, raw_output, mean_brightness, variance, edge_density, created_at FROM detection_history WHERE user_id = ? ORDER BY created_at DESC");
    $stmt->bind_param("s", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $history_list = array();
    
    // Get the base URL for images
    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
    $base_url = $protocol . '://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['SCRIPT_NAME']) . '/';
    
    while ($row = $result->fetch_assoc()) {
        $history_list[] = array(
            "id" => strval($row['id']),
            "user_id" => $row['user_id'],
            "image_url" => $base_url . $row['image_path'],
            "image_path" => $row['image_path'],
            "prediction_result" => $row['prediction_result'],
            "confidence" => floatval($row['confidence']),
            "raw_output" => floatval($row['raw_output']),
            "mean_brightness" => floatval($row['mean_brightness']),
            "variance" => floatval($row['variance']),
            "edge_density" => floatval($row['edge_density']),
            "created_at" => $row['created_at'],
            "timestamp" => strtotime($row['created_at']) * 1000 // Convert to milliseconds for Android
        );
    }
    
    $stmt->close();
    
    echo json_encode(array(
        "status" => "success",
        "message" => "History retrieved successfully",
        "data" => $history_list
    ));
    
} catch (Exception $e) {
    echo json_encode(array("status" => "error", "message" => "Database error: " . $e->getMessage()));
}

$conn->close();
?>
