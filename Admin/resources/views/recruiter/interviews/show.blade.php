@extends('recruiter.layouts.app')

@section('title', 'Interview Details')
@section('page-title', 'Interview Details')

@section('content')
<div class="row">
    <div class="col-lg-8">
        <!-- Interview Information -->
        <div class="card shadow mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Interview Information</h5>
                <span class="badge bg-{{ 
                    $interview->status === 'scheduled' ? 'warning' : 
                    ($interview->status === 'completed' ? 'success' : 
                    ($interview->status === 'cancelled' ? 'danger' : 'secondary')) 
                }} fs-6">
                    {{ ucfirst($interview->status) }}
                </span>
            </div>
            <div class="card-body">
                <div class="row mb-4">
                    <div class="col-md-6">
                        <h6>Candidate Information</h6>
                        <div class="d-flex align-items-center mb-3">
                            @if($interview->user->profile_photo)
                                <img src="{{ asset('storage/' . $interview->user->profile_photo) }}" 
                                     alt="Profile" class="rounded-circle me-3" style="width: 60px; height: 60px;">
                            @else
                                <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center me-3" 
                                     style="width: 60px; height: 60px;">
                                    <span class="text-white h5 mb-0">{{ substr($interview->user->name, 0, 1) }}</span>
                                </div>
                            @endif
                            <div>
                                <h6 class="mb-1">{{ $interview->user->name }}</h6>
                                <small class="text-muted">{{ $interview->user->email }}</small>
                                @if($interview->user->job_title)
                                    <br><small class="text-info">{{ $interview->user->job_title }}</small>
                                @endif
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <h6>Job Information</h6>
                        <p class="mb-1"><strong>{{ $interview->job->title }}</strong></p>
                        <p class="text-muted mb-1">{{ $interview->job->location }}</p>
                        <p class="text-muted">{{ $interview->job->job_type }}</p>
                    </div>
                </div>

                <hr>

                <div class="row mb-4">
                    <div class="col-md-3">
                        <h6>Date</h6>
                        <p>{{ $interview->interview_date->format('l, M d, Y') }}</p>
                    </div>
                    <div class="col-md-3">
                        <h6>Time</h6>
                        <p>{{ $interview->interview_time->format('H:i A') }}</p>
                    </div>
                    <div class="col-md-3">
                        <h6>Type</h6>
                        <p>
                            <i class="fas fa-{{ 
                                $interview->interview_type === 'online' ? 'video' : 
                                ($interview->interview_type === 'offline' ? 'map-marker-alt' : 'phone') 
                            }} me-2"></i>
                            {{ ucfirst($interview->interview_type) }}
                        </p>
                    </div>
                    <div class="col-md-3">
                        <h6>Duration</h6>
                        <p>{{ $interview->interview_time->diffInMinutes($interview->interview_time->copy()->addHour()) }} minutes (estimated)</p>
                    </div>
                </div>

                @if($interview->meeting_link)
                <div class="mb-4">
                    <h6>Meeting Link</h6>
                    <div class="d-flex align-items-center">
                        <a href="{{ $interview->meeting_link }}" target="_blank" class="btn btn-outline-primary me-2">
                            <i class="fas fa-external-link-alt me-2"></i>Join Meeting
                        </a>
                        <code class="bg-light p-2 rounded">{{ $interview->meeting_link }}</code>
                    </div>
                </div>
                @endif

                @if($interview->location)
                <div class="mb-4">
                    <h6>Location</h6>
                    <p>{{ $interview->location }}</p>
                </div>
                @endif

                @if($interview->notes)
                <div class="mb-4">
                    <h6>Interview Notes</h6>
                    <p>{{ $interview->notes }}</p>
                </div>
                @endif

                @if($interview->feedback)
                <div class="mb-4">
                    <h6>Interview Feedback</h6>
                    <p>{{ $interview->feedback }}</p>
                </div>
                @endif

                @if($interview->rating)
                <div class="mb-4">
                    <h6>Rating</h6>
                    <div class="d-flex align-items-center">
                        @for($i = 1; $i <= 5; $i++)
                            <i class="fas fa-star {{ $i <= $interview->rating ? 'text-warning' : 'text-muted' }} me-1"></i>
                        @endfor
                        <span class="ms-2">({{ $interview->rating }}/5)</span>
                    </div>
                </div>
                @endif
            </div>
        </div>

        <!-- Application Details -->
        <div class="card shadow">
            <div class="card-header">
                <h5 class="mb-0">Related Application</h5>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6">
                        <p><strong>Application Status:</strong> 
                            <span class="badge bg-{{ 
                                $interview->application->status === 'applied' ? 'primary' : 
                                ($interview->application->status === 'under_review' ? 'warning' : 
                                ($interview->application->status === 'shortlisted' ? 'success' : 
                                ($interview->application->status === 'hired' ? 'info' : 'secondary'))) 
                            }}">
                                {{ ucfirst(str_replace('_', ' ', $interview->application->status)) }}
                            </span>
                        </p>
                        <p><strong>Applied Date:</strong> {{ $interview->application->created_at->format('M d, Y') }}</p>
                    </div>
                    <div class="col-md-6">
                        @if($interview->application->resume_path)
                        <a href="{{ route('recruiter.applications.download-resume', $interview->application) }}" 
                           class="btn btn-outline-primary">
                            <i class="fas fa-download me-2"></i>Download Resume
                        </a>
                        @endif
                        <a href="{{ route('recruiter.applications.show', $interview->application) }}" 
                           class="btn btn-outline-info">
                            <i class="fas fa-eye me-2"></i>View Full Application
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <!-- Quick Actions -->
        <div class="card shadow mb-4">
            <div class="card-header">
                <h6 class="mb-0">Quick Actions</h6>
            </div>
            <div class="card-body">
                @if($interview->status === 'scheduled')
                    <!-- Update Interview -->
                    <button type="button" class="btn btn-outline-primary w-100 mb-2" data-bs-toggle="modal" data-bs-target="#updateInterviewModal">
                        <i class="fas fa-edit me-2"></i>Update Interview
                    </button>
                    
                    <!-- Mark as Completed -->
                    <button type="button" class="btn btn-success w-100 mb-2" data-bs-toggle="modal" data-bs-target="#completeInterviewModal">
                        <i class="fas fa-check me-2"></i>Mark as Completed
                    </button>
                    
                    <!-- Cancel Interview -->
                    <button type="button" class="btn btn-outline-danger w-100 mb-2" data-bs-toggle="modal" data-bs-target="#cancelInterviewModal">
                        <i class="fas fa-times me-2"></i>Cancel Interview
                    </button>
                @elseif($interview->status === 'completed')
                    <!-- Update Feedback -->
                    <button type="button" class="btn btn-outline-warning w-100 mb-2" data-bs-toggle="modal" data-bs-target="#updateFeedbackModal">
                        <i class="fas fa-edit me-2"></i>Update Feedback
                    </button>
                @endif
                
                <!-- View Candidate Profile -->
                <a href="{{ route('recruiter.candidates.show', $interview->user) }}" class="btn btn-outline-info w-100 mb-2">
                    <i class="fas fa-user me-2"></i>View Candidate Profile
                </a>
                
                <!-- Back to Interviews -->
                <a href="{{ route('recruiter.interviews.index') }}" class="btn btn-outline-secondary w-100">
                    <i class="fas fa-arrow-left me-2"></i>Back to Interviews
                </a>
            </div>
        </div>

        <!-- Interview Timeline -->
        <div class="card shadow">
            <div class="card-header">
                <h6 class="mb-0">Interview Timeline</h6>
            </div>
            <div class="card-body">
                <div class="timeline">
                    <div class="timeline-item completed">
                        <i class="fas fa-calendar-plus"></i>
                        <span>Interview Scheduled</span>
                        <small>{{ $interview->created_at->format('M d, Y H:i A') }}</small>
                    </div>
                    
                    @if($interview->status === 'completed')
                    <div class="timeline-item completed">
                        <i class="fas fa-check-circle"></i>
                        <span>Interview Completed</span>
                        <small>{{ $interview->updated_at->format('M d, Y H:i A') }}</small>
                    </div>
                    @elseif($interview->status === 'cancelled')
                    <div class="timeline-item cancelled">
                        <i class="fas fa-times-circle"></i>
                        <span>Interview Cancelled</span>
                        <small>{{ $interview->updated_at->format('M d, Y H:i A') }}</small>
                    </div>
                    @else
                    <div class="timeline-item active">
                        <i class="fas fa-clock"></i>
                        <span>Scheduled for {{ $interview->interview_date->format('M d, Y') }}</span>
                        <small>{{ $interview->interview_date->diffForHumans() }}</small>
                    </div>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Update Interview Modal -->
<div class="modal fade" id="updateInterviewModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Update Interview</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="{{ route('recruiter.interviews.update', $interview) }}">
                @csrf
                @method('PUT')
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="interview_date" class="form-label">Interview Date</label>
                            <input type="date" class="form-control" id="interview_date" name="interview_date" 
                                   value="{{ $interview->interview_date->format('Y-m-d') }}" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="interview_time" class="form-label">Interview Time</label>
                            <input type="time" class="form-control" id="interview_time" name="interview_time" 
                                   value="{{ $interview->interview_time->format('H:i') }}" required>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="interview_type" class="form-label">Interview Type</label>
                        <select class="form-select" id="interview_type" name="interview_type" required onchange="toggleFields()">
                            <option value="online" {{ $interview->interview_type === 'online' ? 'selected' : '' }}>Online</option>
                            <option value="offline" {{ $interview->interview_type === 'offline' ? 'selected' : '' }}>Offline</option>
                            <option value="phone" {{ $interview->interview_type === 'phone' ? 'selected' : '' }}>Phone</option>
                        </select>
                    </div>
                    
                    <div class="mb-3" id="meeting_link_field">
                        <label for="meeting_link" class="form-label">Meeting Link</label>
                        <input type="url" class="form-control" id="meeting_link" name="meeting_link" 
                               value="{{ $interview->meeting_link }}">
                    </div>
                    
                    <div class="mb-3" id="location_field">
                        <label for="location" class="form-label">Location</label>
                        <input type="text" class="form-control" id="location" name="location" 
                               value="{{ $interview->location }}">
                    </div>
                    
                    <div class="mb-3">
                        <label for="notes" class="form-label">Notes</label>
                        <textarea class="form-control" id="notes" name="notes" rows="3">{{ $interview->notes }}</textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Update Interview</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Complete Interview Modal -->
<div class="modal fade" id="completeInterviewModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Complete Interview</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="{{ route('recruiter.interviews.update-status', $interview) }}">
                @csrf
                @method('PUT')
                <div class="modal-body">
                    <input type="hidden" name="status" value="completed">
                    
                    <div class="mb-3">
                        <label for="rating" class="form-label">Rating (1-5)</label>
                        <select class="form-select" id="rating" name="rating" required>
                            <option value="">Select Rating</option>
                            <option value="1">1 - Poor</option>
                            <option value="2">2 - Below Average</option>
                            <option value="3">3 - Average</option>
                            <option value="4">4 - Good</option>
                            <option value="5">5 - Excellent</option>
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label for="feedback" class="form-label">Interview Feedback</label>
                        <textarea class="form-control" id="feedback" name="feedback" rows="5" 
                                  placeholder="Provide detailed feedback about the interview..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-success">Complete Interview</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Cancel Interview Modal -->
<div class="modal fade" id="cancelInterviewModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Cancel Interview</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="{{ route('recruiter.interviews.cancel', $interview) }}">
                @csrf
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="cancellation_reason" class="form-label">Cancellation Reason</label>
                        <textarea class="form-control" id="cancellation_reason" name="cancellation_reason" rows="4" 
                                  placeholder="Please provide a reason for cancelling this interview..." required></textarea>
                    </div>
                    
                    <div class="alert alert-warning">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        This action will cancel the interview and notify the candidate.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Keep Interview</button>
                    <button type="submit" class="btn btn-danger">Cancel Interview</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<style>
.timeline {
    position: relative;
    padding-left: 30px;
}

.timeline-item {
    position: relative;
    padding-bottom: 20px;
    border-left: 2px solid #e9ecef;
}

.timeline-item:last-child {
    border-left: none;
}

.timeline-item i {
    position: absolute;
    left: -25px;
    top: 0;
    background: #fff;
    border: 2px solid #e9ecef;
    border-radius: 50%;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #6c757d;
}

.timeline-item.completed i {
    background: #28a745;
    border-color: #28a745;
    color: white;
}

.timeline-item.active i {
    background: #ffc107;
    border-color: #ffc107;
    color: white;
}

.timeline-item.cancelled i {
    background: #dc3545;
    border-color: #dc3545;
    color: white;
}

.timeline-item span {
    font-weight: 500;
    display: block;
}

.timeline-item small {
    color: #6c757d;
    font-size: 0.8em;
}
</style>

<script>
function toggleFields() {
    const type = document.getElementById('interview_type').value;
    const meetingField = document.getElementById('meeting_link_field');
    const locationField = document.getElementById('location_field');
    
    if (type === 'online') {
        meetingField.style.display = 'block';
        locationField.style.display = 'none';
    } else if (type === 'offline') {
        meetingField.style.display = 'none';
        locationField.style.display = 'block';
    } else {
        meetingField.style.display = 'none';
        locationField.style.display = 'none';
    }
}

// Initialize field visibility
document.addEventListener('DOMContentLoaded', function() {
    toggleFields();
});
</script>
@endsection