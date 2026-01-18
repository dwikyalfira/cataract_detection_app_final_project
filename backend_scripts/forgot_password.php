<?php
require_once 'db_connect.php';

$response = array();

// Set timezone to Jakarta/Bangkok (UTC+7) to match the user's observed server time
// Or rely on server default if it's already correct (which it seems to be)
date_default_timezone_set('UTC'); 

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['email'])) {
        $email = $_POST['email'];

        // Check if user exists
        $stmt = $conn->prepare("SELECT uid FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            // Generate 6-digit OTP
            $otp = sprintf("%06d", mt_rand(1, 999999));
            
            // Calculate expiry in PHP to ensure consistency
            $expiry = date('Y-m-d H:i:s', strtotime('+15 minutes'));

            // Update user record
            $update_stmt = $conn->prepare("UPDATE users SET reset_token = ?, token_expiry = ? WHERE email = ?");
            $update_stmt->bind_param("sss", $otp, $expiry, $email);

            if ($update_stmt->execute()) {
                $response['status'] = 'success';
                // IMPORTANT: For development/testing only, return the OTP in the response
                $response['message'] = 'OTP generated. Check logs.'; 
                $response['data'] = array('otp' => $otp);
            } else {
                $response['status'] = 'error';
                $response['message'] = 'Failed to generate OTP';
            }
            $update_stmt->close();
        } else {
            $response['status'] = 'error';
            $response['message'] = 'Email not found';
        }
        $stmt->close();
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Email is required';
    }
} else {
    $response['status'] = 'error';
    $response['message'] = 'Invalid request method';
}

echo json_encode($response);
$conn->close();
