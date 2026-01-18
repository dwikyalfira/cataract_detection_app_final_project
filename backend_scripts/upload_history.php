<?php
require_once 'db_connect.php';

header('Content-Type: application/json');

// Check if this is a POST request
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(array("status" => "error", "message" => "Only POST method allowed"));
    exit();
}

// Get form data
$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$prediction_result = isset($_POST['prediction_result']) ? trim($_POST['prediction_result']) : '';
$confidence = isset($_POST['confidence']) ? floatval($_POST['confidence']) : 0;
$raw_output = isset($_POST['raw_output']) ? floatval($_POST['raw_output']) : 0;
$mean_brightness = isset($_POST['mean_brightness']) ? floatval($_POST['mean_brightness']) : 0;
$variance = isset($_POST['variance']) ? floatval($_POST['variance']) : 0;
$edge_density = isset($_POST['edge_density']) ? floatval($_POST['edge_density']) : 0;

// Validate required fields
if (empty($user_id)) {
    echo json_encode(array("status" => "error", "message" => "User ID is required"));
    exit();
}

if (empty($prediction_result)) {
    echo json_encode(array("status" => "error", "message" => "Prediction result is required"));
    exit();
}

// Check if image was uploaded
if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    $error_message = "Image upload failed";
    if (isset($_FILES['image'])) {
        switch ($_FILES['image']['error']) {
            case UPLOAD_ERR_INI_SIZE:
            case UPLOAD_ERR_FORM_SIZE:
                $error_message = "Image file is too large";
                break;
            case UPLOAD_ERR_NO_FILE:
                $error_message = "No image file uploaded";
                break;
            default:
                $error_message = "Image upload error: " . $_FILES['image']['error'];
        }
    }
    echo json_encode(array("status" => "error", "message" => $error_message));
    exit();
}

// Create uploads directory if it doesn't exist
$upload_base_dir = __DIR__ . '/uploads';
$user_upload_dir = $upload_base_dir . '/' . $user_id;

if (!is_dir($upload_base_dir)) {
    if (!mkdir($upload_base_dir, 0755, true)) {
        echo json_encode(array("status" => "error", "message" => "Failed to create uploads directory"));
        exit();
    }
}

if (!is_dir($user_upload_dir)) {
    if (!mkdir($user_upload_dir, 0755, true)) {
        echo json_encode(array("status" => "error", "message" => "Failed to create user uploads directory"));
        exit();
    }
}

// Generate unique filename
$file_extension = strtolower(pathinfo($_FILES['image']['name'], PATHINFO_EXTENSION));
$allowed_extensions = array('jpg', 'jpeg', 'png', 'webp');

if (!in_array($file_extension, $allowed_extensions)) {
    echo json_encode(array("status" => "error", "message" => "Invalid image format. Allowed: jpg, jpeg, png, webp"));
    exit();
}

$unique_filename = 'detection_' . time() . '_' . uniqid() . '.' . $file_extension;
$relative_path = 'uploads/' . $user_id . '/' . $unique_filename;
$full_path = $user_upload_dir . '/' . $unique_filename;

// Move uploaded file
if (!move_uploaded_file($_FILES['image']['tmp_name'], $full_path)) {
    echo json_encode(array("status" => "error", "message" => "Failed to save uploaded image"));
    exit();
}

// Compress image if it's too large (target ~500KB)
$max_size = 500 * 1024; // 500KB
$current_size = filesize($full_path);

if ($current_size > $max_size && in_array($file_extension, array('jpg', 'jpeg', 'png'))) {
    try {
        // Load image based on type
        if ($file_extension === 'png') {
            $image = imagecreatefrompng($full_path);
        } else {
            $image = imagecreatefromjpeg($full_path);
        }
        
        if ($image) {
            // Calculate compression quality
            $quality = min(90, intval(($max_size / $current_size) * 100));
            $quality = max(50, $quality); // Minimum 50% quality
            
            // Save as JPEG for better compression
            $compressed_filename = 'detection_' . time() . '_' . uniqid() . '.jpg';
            $compressed_relative_path = 'uploads/' . $user_id . '/' . $compressed_filename;
            $compressed_full_path = $user_upload_dir . '/' . $compressed_filename;
            
            imagejpeg($image, $compressed_full_path, $quality);
            imagedestroy($image);
            
            // If compression successful, use compressed file
            if (file_exists($compressed_full_path)) {
                unlink($full_path); // Delete original
                $full_path = $compressed_full_path;
                $relative_path = $compressed_relative_path;
                $unique_filename = $compressed_filename;
            }
        }
    } catch (Exception $e) {
        // Compression failed, continue with original file
        error_log("Image compression failed: " . $e->getMessage());
    }
}

// Insert into database
try {
    $stmt = $conn->prepare("INSERT INTO detection_history (user_id, image_path, prediction_result, confidence, raw_output, mean_brightness, variance, edge_density) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("sssddddd", $user_id, $relative_path, $prediction_result, $confidence, $raw_output, $mean_brightness, $variance, $edge_density);
    
    if ($stmt->execute()) {
        $history_id = $stmt->insert_id;
        
        // Also update user stats
        $is_healthy = (strtolower($prediction_result) === 'normal') ? 1 : 0;
        
        if ($is_healthy) {
            $update_stmt = $conn->prepare("UPDATE users SET total_scans = total_scans + 1, healthy_scans = healthy_scans + 1 WHERE uid = ?");
        } else if (strtolower($prediction_result) === 'cataract') {
            $update_stmt = $conn->prepare("UPDATE users SET total_scans = total_scans + 1, alert_scans = alert_scans + 1 WHERE uid = ?");
        } else {
            $update_stmt = $conn->prepare("UPDATE users SET total_scans = total_scans + 1 WHERE uid = ?");
        }
        
        if ($update_stmt) {
            $update_stmt->bind_param("s", $user_id);
            $update_stmt->execute();
            $update_stmt->close();
        }
        
        echo json_encode(array(
            "status" => "success",
            "message" => "History saved successfully",
            "data" => array(
                "history_id" => strval($history_id),
                "image_path" => $relative_path
            )
        ));
    } else {
        // Delete uploaded file on database error
        if (file_exists($full_path)) {
            unlink($full_path);
        }
        echo json_encode(array("status" => "error", "message" => "Failed to save history to database"));
    }
    
    $stmt->close();
} catch (Exception $e) {
    // Delete uploaded file on error
    if (file_exists($full_path)) {
        unlink($full_path);
    }
    echo json_encode(array("status" => "error", "message" => "Database error: " . $e->getMessage()));
}

$conn->close();
?>
