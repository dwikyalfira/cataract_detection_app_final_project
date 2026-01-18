<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "cataract_db";

// Enable error reporting for debugging (remove in production)
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

try {
    // Create connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    $conn->set_charset("utf8mb4");
} catch (Exception $e) {
    header('Content-Type: application/json');
    $error_response = array(
        "status" => "error", 
        "message" => "Connection failed: " . $e->getMessage()
    );
    echo json_encode($error_response);
    exit();
}
?>
