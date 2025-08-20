<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Interview Invitation</title>
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
        .interview-details {
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
        .btn-success {
            background: #28a745;
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
    <div class="header">
        <h1>🎉 Interview Invitation</h1>
        <p>{{ $recruiter->company_name }}</p>
    </div>
    
    <div class="content">
        <h2>Dear {{ $candidate->name }},</h2>
        
        <p>Congratulations! We are pleased to invite you for an interview for the position of <strong>{{ $job->title }}</strong> at {{ $recruiter->company_name }}.</p>
        
        <div class="interview-details">
            <h3>📅 Interview Details</h3>
            
            <div class="detail-row">
                <span class="detail-label">Position:</span>
                <span>{{ $job->title }}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label">Date:</span>
                <span>{{ $interview->interview_date->format('l, F j, Y') }}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label">Time:</span>
                <span>{{ $interview->interview_time->format('g:i A') }}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label">Type:</span>
                <span>{{ ucfirst($interview->interview_type) }} Interview</span>
            </div>
            
            @if($interview->interview_type === 'online' && $interview->meeting_link)
            <div class="detail-row">
                <span class="detail-label">Meeting Link:</span>
                <span><a href="{{ $interview->meeting_link }}" target="_blank">{{ $interview->meeting_link }}</a></span>
            </div>
            @endif
            
            @if($interview->interview_type === 'offline' && $interview->location)
            <div class="detail-row">
                <span class="detail-label">Location:</span>
                <span>{{ $interview->location }}</span>
            </div>
            @endif
            
            <div class="detail-row">
                <span class="detail-label">Interviewer:</span>
                <span>{{ $recruiter->name }} ({{ $recruiter->designation }})</span>
            </div>
        </div>
        
        @if($interview->notes)
        <div style="background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <h4>📝 Additional Notes:</h4>
            <p>{{ $interview->notes }}</p>
        </div>
        @endif
        
        <div style="text-align: center; margin: 30px 0;">
            @if($interview->interview_type === 'online' && $interview->meeting_link)
            <a href="{{ $interview->meeting_link }}" class="btn btn-success">Join Interview</a>
            @endif
            <a href="mailto:{{ $recruiter->email }}" class="btn">Contact Recruiter</a>
        </div>
        
        <h3>📋 What to Expect:</h3>
        <ul>
            <li>The interview will last approximately 45-60 minutes</li>
            <li>We'll discuss your experience and the role requirements</li>
            <li>You'll have the opportunity to ask questions about the position and company</li>
            <li>Please bring a copy of your resume and any relevant portfolio items</li>
        </ul>
        
        <h3>📞 Need to Reschedule?</h3>
        <p>If you need to reschedule this interview, please contact us as soon as possible at <a href="mailto:{{ $recruiter->email }}">{{ $recruiter->email }}</a> or {{ $recruiter->mobile }}.</p>
        
        <p>We look forward to meeting with you and learning more about your qualifications for this exciting opportunity!</p>
        
        <p>Best regards,<br>
        <strong>{{ $recruiter->name }}</strong><br>
        {{ $recruiter->designation }}<br>
        {{ $recruiter->company_name }}<br>
        📧 {{ $recruiter->email }}<br>
        📱 {{ $recruiter->mobile }}</p>
    </div>
    
    <div class="footer">
        <p>This is an automated message from {{ $recruiter->company_name }}. Please do not reply to this email.</p>
        <p>If you have any questions, please contact us directly at {{ $recruiter->email }}</p>
    </div>
</body>
</html>