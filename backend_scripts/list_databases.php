<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";

// Create connection without DB
try {
    $conn = new mysqli($servername, $username, $password);
    
    $result = $conn->query("SHOW DATABASES");
    $databases = [];
    while($row = $result->fetch_array()) {
        $databases[] = $row[0];
    }
    
    echo json_encode(array(
        "status" => "success",
        "message" => "Connected to MySQL successfully",
        "databases" => $databases
    ));
    
} catch (Exception $e) {
    echo json_encode(array(
        "status" => "error",
        "message" => "Connection failed: " . $e->getMessage()
    ));
}
?>
