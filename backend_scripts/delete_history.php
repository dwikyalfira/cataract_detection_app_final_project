<?php
require_once 'db_connect.php';

header('Content-Type: application/json');

// Check if this is a POST request
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(array("status" => "error", "message" => "Only POST method allowed"));
    exit();
}

// Get POST data
$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$history_id = isset($_POST['history_id']) ? trim($_POST['history_id']) : '';

if (empty($user_id)) {
    echo json_encode(array("status" => "error", "message" => "User ID is required"));
    exit();
}

if (empty($history_id)) {
    echo json_encode(array("status" => "error", "message" => "History ID is required"));
    exit();
}

try {
    // First, get the history record to find the image path and prediction result
    $stmt = $conn->prepare("SELECT image_path, prediction_result FROM detection_history WHERE id = ? AND user_id = ?");
    $stmt->bind_param("ss", $history_id, $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        echo json_encode(array("status" => "error", "message" => "History not found or access denied"));
        $stmt->close();
        $conn->close();
        exit();
    }
    
    $row = $result->fetch_assoc();
    $image_path = $row['image_path'];
    $prediction_result = $row['prediction_result'];
    $stmt->close();
    
    // Delete the database record
    $delete_stmt = $conn->prepare("DELETE FROM detection_history WHERE id = ? AND user_id = ?");
    $delete_stmt->bind_param("ss", $history_id, $user_id);
    
    if ($delete_stmt->execute()) {
        // Delete the image file
        $full_path = __DIR__ . '/' . $image_path;
        if (file_exists($full_path)) {
            unlink($full_path);
        }
        
        // Update user stats (decrement counters)
        $is_healthy = strtolower($prediction_result) === 'normal';
        $is_cataract = strtolower($prediction_result) === 'cataract';
        
        if ($is_healthy) {
            $update_stmt = $conn->prepare("UPDATE users SET total_scans = GREATEST(0, total_scans - 1), healthy_scans = GREATEST(0, healthy_scans - 1) WHERE uid = ?");
        } else if ($is_cataract) {
            $update_stmt = $conn->prepare("UPDATE users SET total_scans = GREATEST(0, total_scans - 1), alert_scans = GREATEST(0, alert_scans - 1) WHERE uid = ?");
        } else {
            $update_stmt = $conn->prepare("UPDATE users SET total_scans = GREATEST(0, total_scans - 1) WHERE uid = ?");
        }
        
        if ($update_stmt) {
            $update_stmt->bind_param("s", $user_id);
            $update_stmt->execute();
            $update_stmt->close();
        }
        
        echo json_encode(array(
            "status" => "success",
            "message" => "History deleted successfully"
        ));
    } else {
        echo json_encode(array("status" => "error", "message" => "Failed to delete history"));
    }
    
    $delete_stmt->close();
    
} catch (Exception $e) {
    echo json_encode(array("status" => "error", "message" => "Database error: " . $e->getMessage()));
}

$conn->close();
?>
