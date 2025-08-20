<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Application Status Update</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .header {
            background: #007bff;
            color: white;
            padding: 20px;
            text-align: center;
            border-radius: 8px 8px 0 0;
        }
        .content {
            background: #f8f9fa;
            padding: 30px;
            border-radius: 0 0 8px 8px;
        }
        .status-update {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
            text-align: center;
        }
        .status-badge {
            display: inline-block;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: bold;
            margin: 5px;
        }
        .status-applied { background: #e3f2fd; color: #1976d2; }
        .status-under-review { background: #fff3e0; color: #f57c00; }
        .status-shortlisted { background: #e8f5e8; color: #388e3c; }
        .status-rejected { background: #ffebee; color: #d32f2f; }
        .status-hired { background: #e8f5e8; color: #2e7d32; }
        
        .job-details {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
            border-left: 4px solid #007bff;
        }
        .detail-row {
            display: flex;
            justify-content: space-between;
            margin: 10px 0;
            padding: 8px 0;
            border-bottom: 1px solid #eee;
        }
        .detail-label {
            font-weight: bold;
            color: #666;
        }
        .btn {
            display: inline-block;
            padding: 12px 24px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 10px 5px;
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            color: #666;
            font-size: 14px;
        }
        .next-steps {
            background: #e3f2fd;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>📬 Application Status Update</h1>
        <p>{{ $recruiter->company_name }}</p>
    </div>
    
    <div class="content">
        <h2>Dear {{ $candidate->name }},</h2>
        
        <p>We wanted to update you on the status of your application for the <strong>{{ $job->title }}</strong> position at {{ $recruiter->company_name }}.</p>
        
        <div class="status-update">
            <h3>Status Update</h3>
            @if($oldStatus)
            <div>
                <span class="status-badge status-{{ strtolower(str_replace(' ', '-', $oldStatus)) }}">{{ $oldStatus }}</span>
                <span style="font-size: 24px; margin: 0 10px;">→</span>
            </div>
            @endif
            <div>
                <span class="status-badge status-{{ strtolower(str_replace(' ', '-', $application->status)) }}">{{ $application->status }}</span>
            </div>
        </div>
        
        <div class="job-details">
            <h3>📋 Application Details</h3>
            
            <div class="detail-row">
                <span class="detail-label">Position:</span>
                <span>{{ $job->title }}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label">Company:</span>
                <span>{{ $recruiter->company_name }}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label">Application Date:</span>
                <span>{{ $application->created_at->format('F j, Y') }}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label">Current Status:</span>
                <span><strong>{{ $application->status }}</strong></span>
            </div>
        </div>
        
        @if($application->notes)
        <div style="background: #fff3e0; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <h4>💬 Message from Recruiter:</h4>
            <p>{{ $application->notes }}</p>
        </div>
        @endif
        
        <div class="next-steps">
            <h3>🚀 What's Next?</h3>
            @if($application->status === 'Applied')
                <p>Thank you for your application! Our team is currently reviewing all applications. We'll be in touch soon with next steps.</p>
            @elseif($application->status === 'Under Review')
                <p>Great news! Your application has caught our attention and is currently under detailed review by our hiring team.</p>
            @elseif($application->status === 'Shortlisted')
                <p>Congratulations! You've been shortlisted for this position. We'll be contacting you soon to schedule an interview.</p>
            @elseif($application->status === 'Rejected')
                <p>Thank you for your interest in this position. While we won't be moving forward with your application at this time, we encourage you to apply for other opportunities that match your skills.</p>
            @elseif($application->status === 'Hired')
                <p>🎉 Congratulations! We're excited to offer you the position. Our HR team will be in touch with you shortly regarding next steps and onboarding.</p>
            @endif
        </div>
        
        <div style="text-align: center; margin: 30px 0;">
            <a href="mailto:{{ $recruiter->email }}" class="btn">Contact Recruiter</a>
        </div>
        
        @if($application->status === 'Shortlisted')
        <div style="background: #e8f5e8; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <h4>📞 Interview Preparation Tips:</h4>
            <ul>
                <li>Review the job description and requirements</li>
                <li>Research our company and recent news</li>
                <li>Prepare examples of your relevant experience</li>
                <li>Think of questions you'd like to ask us</li>
            </ul>
        </div>
        @endif
        
        <p>Thank you for your continued interest in {{ $recruiter->company_name }}. We appreciate the time you've invested in the application process.</p>
        
        <p>Best regards,<br>
        <strong>{{ $recruiter->name }}</strong><br>
        {{ $recruiter->designation }}<br>
        {{ $recruiter->company_name }}<br>
        📧 {{ $recruiter->email }}</p>
    </div>
    
    <div class="footer">
        <p>This is an automated message from {{ $recruiter->company_name }}. Please do not reply to this email.</p>
        <p>If you have any questions, please contact us directly at {{ $recruiter->email }}</p>
    </div>
</body>
</html>