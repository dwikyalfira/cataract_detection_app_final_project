<?php
require_once 'db_connect.php';

$response = array();

// Function to check if column exists
function columnExists($conn, $table, $column) {
    $result = $conn->query("SHOW COLUMNS FROM `$table` LIKE '$column'");
    return $result && $result->num_rows > 0;
}

$table = 'users';
$messages = [];

// Check and add reset_token
if (!columnExists($conn, $table, 'reset_token')) {
    $sql = "ALTER TABLE `$table` ADD `reset_token` VARCHAR(6) DEFAULT NULL";
    if ($conn->query($sql) === TRUE) {
        $messages[] = "Column 'reset_token' added.";
    } else {
        $messages[] = "Error adding 'reset_token': " . $conn->error;
    }
} else {
    $messages[] = "Column 'reset_token' exists.";
}

// Check and add token_expiry
if (!columnExists($conn, $table, 'token_expiry')) {
    $sql = "ALTER TABLE `$table` ADD `token_expiry` DATETIME DEFAULT NULL";
    if ($conn->query($sql) === TRUE) {
        $messages[] = "Column 'token_expiry' added.";
    } else {
        $messages[] = "Error adding 'token_expiry': " . $conn->error;
    }
} else {
    $messages[] = "Column 'token_expiry' exists.";
}

$response['status'] = 'success';
$response['message'] = implode(" ", $messages);

echo json_encode($response);
$conn->close();
?>
