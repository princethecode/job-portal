<!DOCTYPE html>
<html>
<head>
    <meta charset=\"utf-8\">
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
    <title>Job Posting {{ ucfirst($status) }}</title>
    <style>
        body {
            font-family: 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .header {
            background: {{ $status === 'approved' ? '#4CAF50' : '#f44336' }};
            color: white;
            padding: 20px;
            text-align: center;
            border-radius: 8px 8px 0 0;
        }
        .content {
            background: #f9f9f9;
            padding: 30px;
            border-radius: 0 0 8px 8px;
            border: 1px solid #ddd;
            border-top: none;
        }
        .job-details {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
            border-left: 4px solid {{ $status === 'approved' ? '#4CAF50' : '#f44336' }};
        }
        .status-badge {
            display: inline-block;
            padding: 8px 16px;
            border-radius: 20px;
            color: white;
            font-weight: bold;
            background: {{ $status === 'approved' ? '#4CAF50' : '#f44336' }};
            text-transform: uppercase;
            font-size: 12px;
        }
        .decline-reason {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 8px;
            padding: 15px;
            margin: 20px 0;
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            color: #666;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class=\"header\">
        <h1>Job Posting {{ ucfirst($status) }}</h1>
    </div>
    
    <div class=\"content\">
        <p>Dear {{ $job->recruiter->company_name ?? 'Recruiter' }},</p>
        
        @if($status === 'approved')
            <p>Great news! Your job posting has been <strong>approved</strong> and is now live on our platform.</p>
        @else
            <p>We have reviewed your job posting and unfortunately it has been <strong>declined</strong>.</p>
        @endif
        
        <div class=\"job-details\">
            <h3>{{ $job->title }}</h3>
            <p><strong>Company:</strong> {{ $job->company_name }}</p>
            <p><strong>Location:</strong> {{ $job->location }}</p>
            <p><strong>Job Type:</strong> {{ $job->job_type }}</p>
            <p><strong>Category:</strong> {{ $job->category }}</p>
            <p><strong>Posted:</strong> {{ $job->created_at->format('M d, Y') }}</p>
            <p><strong>Status:</strong> <span class=\"status-badge\">{{ $status }}</span></p>
        </div>
        
        @if($status === 'approved')
            <p>Your job posting is now visible to job seekers and they can start applying. You will receive notifications when new applications are submitted.</p>
            
            <p><strong>Next Steps:</strong></p>
            <ul>
                <li>Monitor applications through your recruiter dashboard</li>
                <li>Review and respond to candidates promptly</li>
                <li>Schedule interviews with qualified applicants</li>
            </ul>
        @endif
        
        @if($status === 'declined' && $reason)
            <div class=\"decline-reason\">
                <h4>Reason for Decline:</h4>
                <p>{{ $reason }}</p>
            </div>
            
            <p>Please review the feedback above and feel free to resubmit your job posting after making the necessary adjustments.</p>
        @endif
        
        <p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
        
        <p>Best regards,<br>
        <strong>Job Portal Team</strong></p>
    </div>
    
    <div class=\"footer\">
        <p>This is an automated email. Please do not reply to this message.</p>
        <p>&copy; {{ date('Y') }} Job Portal. All rights reserved.</p>
    </div>
</body>
</html>