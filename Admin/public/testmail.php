<?php
$to = "nareshgupta181@gmail.com";
$subject = "Test Email from PHP";
$message = "This is a test email sent from PHP. If you receive this, then PHP's mail function is working.";
$headers = "From: noreply@emps.co.in\r\n";
$headers .= "Reply-To: noreply@emps.co.in\r\n";
$headers .= "Content-Type: text/plain; charset=UTF-8\r\n";

$result = mail($to, $subject, $message, $headers);

if ($result) {
    echo "Email sent successfully!";
} else {
    echo "Email sending failed. Error: " . error_get_last()['message'];
}