<?php
require_once 'db_connect.php';

$response = array();

$table = 'users';
$result = $conn->query("DESCRIBE `$table`");

if ($result) {
    $columns = [];
    while ($row = $result->fetch_assoc()) {
        $columns[] = $row;
    }
    $response['status'] = 'success';
    $response['columns'] = $columns;
} else {
    $response['status'] = 'error';
    $response['message'] = "Error describing table: " . $conn->error;
}

echo json_encode($response);
$conn->close();
?>
