<?php

namespace App\Mail;

use App\Models\Job;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class JobApprovalUpdate extends Mailable
{
    use Queueable, SerializesModels;

    public $job;
    public $status;
    public $reason;

    /**
     * Create a new message instance.
     */
    public function __construct(Job $job, string $status, string $reason = null)
    {
        $this->job = $job;
        $this->status = $status;
        $this->reason = $reason;
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        $subject = $this->status === 'approved' 
            ? 'Job Posting Approved - ' . $this->job->title
            : 'Job Posting Update - ' . $this->job->title;

        return new Envelope(
            subject: $subject,
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            view: 'emails.job-approval-update',
            with: [
                'job' => $this->job,
                'status' => $this->status,
                'reason' => $this->reason,
            ],
        );
    }

    /**
     * Get the attachments for the message.
     *
     * @return array<int, \Illuminate\Mail\Mailables\Attachment>
     */
    public function attachments(): array
    {
        return [];
    }
}