<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Password Reset</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .container {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 25px;
            background-color: #f9f9f9;
        }
        .logo {
            text-align: center;
            margin-bottom: 20px;
        }
        .logo img {
            max-width: 150px;
            height: auto;
        }
        .header {
            background-color: #4A6CF7;
            color: white;
            padding: 15px;
            text-align: center;
            border-radius: 4px;
            margin-bottom: 20px;
        }
        .content {
            background-color: white;
            padding: 20px;
            border-radius: 4px;
            margin-bottom: 20px;
        }
        .button {
            display: inline-block;
            background-color: #4A6CF7;
            color: white;
            text-decoration: none;
            padding: 12px 25px;
            border-radius: 4px;
            margin: 15px 0;
            font-weight: bold;
        }
        .info {
            background-color: #f1f1f1;
            padding: 15px;
            border-radius: 4px;
            font-size: 14px;
            margin-bottom: 20px;
        }
        .footer {
            text-align: center;
            color: #777;
            font-size: 12px;
            margin-top: 30px;
        }
        .warning {
            color: #e74c3c;
            margin-top: 20px;
            font-style: italic;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">
            <!-- Replace with your company logo -->
            <h2>Job Portal</h2>
        </div>
        
        <div class="header">
            <h2>Password Reset Request</h2>
        </div>
        
        <div class="content">
            <p>Hello <strong>{{ $user->name }}</strong>,</p>
            
            <p>We received a request to reset your password for your Job Portal account. If you didn't make this request, you can safely ignore this email.</p>
            
            <p>To reset your password, click on the button below:</p>
            
            <div style="text-align: center;">
                <a href="{{ $resetUrl }}" class="button">Reset Password</a>
            </div>
            
            <p>Or copy and paste the following URL into your browser:</p>
            <p style="word-break: break-all; font-size: 14px;">{{ $resetUrl }}</p>
            
            <p>This password reset link will expire on <strong>{{ $expiresAt }}</strong>.</p>
            
            <div class="warning">
                <p>If you did not request a password reset, please ignore this email or contact support if you have concerns about your account security.</p>
            </div>
        </div>
        
        <div class="info">
            <p><strong>Account Information:</strong></p>
            <p>Username/Email: {{ $user->email }}</p>
            @if($user->mobile)
            <p>Registered Mobile: {{ $user->mobile }}</p>
            @endif
        </div>
        
        <div class="footer">
            <p>&copy; {{ date('Y') }} Job Portal. All rights reserved.</p>
            <p>This is an automated email, please do not reply to this message.</p>
        </div>
    </div>
</body>
</html> 