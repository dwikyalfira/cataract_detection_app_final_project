<?php
require_once 'db_connect.php';

$response = array();

// Set timezone to Jakarta (UTC+7) to match the user's location
date_default_timezone_set('UTC');

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['email']) && isset($_POST['otp']) && isset($_POST['new_password'])) {
        $email = $_POST['email'];
        $otp = $_POST['otp'];
        $new_password = $_POST['new_password'];

        // Get current time in PHP
        $current_time = date('Y-m-d H:i:s');

        // Debug: Check what's in the DB
        $debug_stmt = $conn->prepare("SELECT reset_token, token_expiry FROM users WHERE email = ?");
        $debug_stmt->bind_param("s", $email);
        $debug_stmt->execute();
        $debug_result = $debug_stmt->get_result();
        
        if ($debug_result->num_rows > 0) {
            $row = $debug_result->fetch_assoc();
            $db_token = $row['reset_token'];
            $db_expiry = $row['token_expiry'];
            
            // Strict comparison
            if ((string)$db_token !== (string)$otp) {
                $response['status'] = 'error';
                $response['message'] = "Invalid OTP";
            } elseif ($db_expiry == null) {
                 $response['status'] = 'error';
                 $response['message'] = "OTP Expired (Null)";
            } elseif (strtotime($db_expiry) <= strtotime($current_time)) {
                $response['status'] = 'error';
                $response['message'] = "OTP Expired";
            } else {
                // OTP is valid and not expired, proceed to update
                 $hashed_password = password_hash($new_password, PASSWORD_DEFAULT);
            
                // Update password and clear OTP
                $update_stmt = $conn->prepare("UPDATE users SET password = ?, reset_token = NULL, token_expiry = NULL WHERE email = ?");
                $update_stmt->bind_param("ss", $hashed_password, $email);
    
                if ($update_stmt->execute()) {
                    $response['status'] = 'success';
                    $response['message'] = 'Password reset successfully';
                } else {
                    $response['status'] = 'error';
                    $response['message'] = 'Failed to update password';
                }
                $update_stmt->close();
            }
        } else {
             $response['status'] = 'error';
             $response['message'] = 'User not found';
        }
        $debug_stmt->close();
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Missing required fields';
    }
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid request method';
}

echo json_encode($response);
$conn->close();
