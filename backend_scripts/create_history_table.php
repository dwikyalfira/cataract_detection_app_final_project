<?php
require_once 'db_connect.php';

header('Content-Type: application/json');

// SQL to create the detection_history table
$sql = "CREATE TABLE IF NOT EXISTS detection_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    prediction_result VARCHAR(50) NOT NULL,
    confidence FLOAT NOT NULL DEFAULT 0,
    raw_output FLOAT DEFAULT 0,
    mean_brightness FLOAT DEFAULT 0,
    variance FLOAT DEFAULT 0,
    edge_density FLOAT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
)";

try {
    if ($conn->query($sql) === TRUE) {
        echo json_encode(array(
            "status" => "success",
            "message" => "detection_history table created successfully or already exists"
        ));
    } else {
        echo json_encode(array(
            "status" => "error",
            "message" => "Error creating table: " . $conn->error
        ));
    }
} catch (Exception $e) {
    echo json_encode(array(
        "status" => "error",
        "message" => "Database error: " . $e->getMessage()
    ));
}

$conn->close();
?>
