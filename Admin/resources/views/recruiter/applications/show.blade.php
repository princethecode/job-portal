@extends('recruiter.layouts.app')

@section('title', 'Application Details')
@section('page-title', 'Application Details')

@section('content')
<div class="row">
    <div class="col-lg-8">
        <!-- Candidate Information -->
        <div class="card shadow mb-4">
            <div class="card-header">
                <h5 class="mb-0">Candidate Information</h5>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-3 text-center">
                        @if($application->user->profile_photo)
                            <img src="{{ asset('storage/' . $application->user->profile_photo) }}" 
                                 alt="Profile" class="rounded-circle mb-3" style="width: 120px; height: 120px;">
                        @else
                            <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center mb-3 mx-auto" 
                                 style="width: 120px; height: 120px;">
                                <span class="text-white h2">{{ substr($application->user->name, 0, 1) }}</span>
                            </div>
                        @endif
                    </div>
                    <div class="col-md-9">
                        <h4>{{ $application->user->name }}</h4>
                        <p class="text-muted mb-2">
                            <i class="fas fa-envelope me-2"></i>{{ $application->user->email }}
                        </p>
                        @if($application->user->mobile)
                        <p class="text-muted mb-2">
                            <i class="fas fa-phone me-2"></i>{{ $application->user->mobile }}
                        </p>
                        @endif
                        @if($application->user->location)
                        <p class="text-muted mb-2">
                            <i class="fas fa-map-marker-alt me-2"></i>{{ $application->user->location }}
                        </p>
                        @endif
                        @if($application->user->job_title)
                        <p class="text-muted mb-2">
                            <i class="fas fa-briefcase me-2"></i>{{ $application->user->job_title }}
                        </p>
                        @endif
                        @if($application->user->current_company)
                        <p class="text-muted mb-2">
                            <i class="fas fa-building me-2"></i>{{ $application->user->current_company }}
                        </p>
                        @endif
                    </div>
                </div>

                @if($application->user->about_me)
                <hr>
                <h6>About</h6>
                <p>{{ $application->user->about_me }}</p>
                @endif

                @if($application->user->skills)
                <hr>
                <h6>Skills</h6>
                <p>{{ $application->user->skills }}</p>
                @endif

                @if($application->user->experiences->count() > 0)
                <hr>
                <h6>Experience</h6>
                @foreach($application->user->experiences as $experience)
                <div class="mb-3">
                    <strong>{{ $experience->job_title }}</strong> at {{ $experience->company }}
                    <br>
                    <small class="text-muted">{{ $experience->start_date }} - {{ $experience->end_date ?? 'Present' }}</small>
                    @if($experience->description)
                    <p class="mt-1">{{ $experience->description }}</p>
                    @endif
                </div>
                @endforeach
                @endif
            </div>
        </div>

        <!-- Application Details -->
        <div class="card shadow mb-4">
            <div class="card-header">
                <h5 class="mb-0">Application Details</h5>
            </div>
            <div class="card-body">
                <div class="row mb-3">
                    <div class="col-md-6">
                        <strong>Job Applied For:</strong>
                        <br>
                        {{ $application->job->title }}
                    </div>
                    <div class="col-md-6">
                        <strong>Application Date:</strong>
                        <br>
                        {{ $application->created_at->format('M d, Y H:i A') }}
                    </div>
                </div>

                @if($application->cover_letter)
                <div class="mb-3">
                    <strong>Cover Letter:</strong>
                    <p class="mt-2">{{ $application->cover_letter }}</p>
                </div>
                @endif

                @if($application->resume_path)
                <div class="mb-3">
                    <strong>Resume:</strong>
                    <br>
                    <a href="{{ route('recruiter.applications.download-resume', $application) }}" 
                       class="btn btn-outline-primary btn-sm mt-2">
                        <i class="fas fa-download me-2"></i>Download Resume
                    </a>
                </div>
                @endif

                @if($application->notes)
                <div class="mb-3">
                    <strong>Notes:</strong>
                    <p class="mt-2">{{ $application->notes }}</p>
                </div>
                @endif
            </div>
        </div>

        <!-- Interviews -->
        @if($application->interviews->count() > 0)
        <div class="card shadow">
            <div class="card-header">
                <h5 class="mb-0">Interview History</h5>
            </div>
            <div class="card-body">
                @foreach($application->interviews as $interview)
                <div class="border rounded p-3 mb-3">
                    <div class="row">
                        <div class="col-md-6">
                            <strong>Interview Date:</strong> {{ $interview->interview_date->format('M d, Y') }}
                            <br>
                            <strong>Time:</strong> {{ $interview->interview_time->format('H:i A') }}
                            <br>
                            <strong>Type:</strong> {{ ucfirst($interview->interview_type) }}
                        </div>
                        <div class="col-md-6">
                            <strong>Status:</strong> 
                            <span class="badge bg-{{ $interview->status === 'scheduled' ? 'warning' : ($interview->status === 'completed' ? 'success' : 'secondary') }}">
                                {{ ucfirst($interview->status) }}
                            </span>
                            @if($interview->rating)
                            <br>
                            <strong>Rating:</strong> 
                            @for($i = 1; $i <= 5; $i++)
                                <i class="fas fa-star {{ $i <= $interview->rating ? 'text-warning' : 'text-muted' }}"></i>
                            @endfor
                            @endif
                        </div>
                    </div>
                    
                    @if($interview->meeting_link)
                    <div class="mt-2">
                        <strong>Meeting Link:</strong> 
                        <a href="{{ $interview->meeting_link }}" target="_blank">{{ $interview->meeting_link }}</a>
                    </div>
                    @endif
                    
                    @if($interview->location)
                    <div class="mt-2">
                        <strong>Location:</strong> {{ $interview->location }}
                    </div>
                    @endif
                    
                    @if($interview->notes)
                    <div class="mt-2">
                        <strong>Notes:</strong> {{ $interview->notes }}
                    </div>
                    @endif
                    
                    @if($interview->feedback)
                    <div class="mt-2">
                        <strong>Feedback:</strong> {{ $interview->feedback }}
                    </div>
                    @endif
                </div>
                @endforeach
            </div>
        </div>
        @endif
    </div>

    <div class="col-lg-4">
        <!-- Quick Actions -->
        <div class="card shadow mb-4">
            <div class="card-header">
                <h6 class="mb-0">Quick Actions</h6>
            </div>
            <div class="card-body">
                <!-- Update Status -->
                <form method="POST" action="{{ route('recruiter.applications.update-status', $application) }}" class="mb-3">
                    @csrf
                    @method('PUT')
                    <div class="mb-3">
                        <label for="status" class="form-label">Update Status</label>
                        <select class="form-select" id="status" name="status" required>
                            <option value="applied" {{ $application->status === 'applied' ? 'selected' : '' }}>Applied</option>
                            <option value="under_review" {{ $application->status === 'under_review' ? 'selected' : '' }}>Under Review</option>
                            <option value="shortlisted" {{ $application->status === 'shortlisted' ? 'selected' : '' }}>Shortlisted</option>
                            <option value="rejected" {{ $application->status === 'rejected' ? 'selected' : '' }}>Rejected</option>
                            <option value="hired" {{ $application->status === 'hired' ? 'selected' : '' }}>Hired</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="notes" class="form-label">Notes (Optional)</label>
                        <textarea class="form-control" id="notes" name="notes" rows="3">{{ $application->notes }}</textarea>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="fas fa-save me-2"></i>Update Status
                    </button>
                </form>

                <hr>

                <!-- Schedule Interview -->
                <button type="button" class="btn btn-success w-100 mb-2" data-bs-toggle="modal" data-bs-target="#scheduleInterviewModal">
                    <i class="fas fa-calendar-plus me-2"></i>Schedule Interview
                </button>

                @if($application->resume_path)
                <a href="{{ route('recruiter.applications.download-resume', $application) }}" class="btn btn-outline-primary w-100 mb-2">
                    <i class="fas fa-download me-2"></i>Download Resume
                </a>
                @endif

                <a href="{{ route('recruiter.candidates.show', $application->user) }}" class="btn btn-outline-info w-100">
                    <i class="fas fa-user me-2"></i>View Full Profile
                </a>
            </div>
        </div>

        <!-- Application Status -->
        <div class="card shadow">
            <div class="card-header">
                <h6 class="mb-0">Application Status</h6>
            </div>
            <div class="card-body">
                <div class="text-center">
                    <span class="badge bg-{{ 
                        $application->status === 'applied' ? 'primary' : 
                        ($application->status === 'under_review' ? 'warning' : 
                        ($application->status === 'shortlisted' ? 'success' : 
                        ($application->status === 'hired' ? 'info' : 'secondary'))) 
                    }} fs-6 mb-3">
                        {{ ucfirst(str_replace('_', ' ', $application->status)) }}
                    </span>
                </div>
                
                <div class="timeline">
                    <div class="timeline-item {{ $application->status !== 'applied' ? 'completed' : 'active' }}">
                        <i class="fas fa-file-alt"></i>
                        <span>Applied</span>
                        <small>{{ $application->created_at->format('M d, Y') }}</small>
                    </div>
                    <div class="timeline-item {{ in_array($application->status, ['under_review', 'shortlisted', 'hired']) ? 'completed' : ($application->status === 'under_review' ? 'active' : '') }}">
                        <i class="fas fa-eye"></i>
                        <span>Under Review</span>
                    </div>
                    <div class="timeline-item {{ in_array($application->status, ['shortlisted', 'hired']) ? 'completed' : ($application->status === 'shortlisted' ? 'active' : '') }}">
                        <i class="fas fa-star"></i>
                        <span>Shortlisted</span>
                    </div>
                    <div class="timeline-item {{ $application->status === 'hired' ? 'completed' : '' }}">
                        <i class="fas fa-check-circle"></i>
                        <span>Hired</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Schedule Interview Modal -->
<div class="modal fade" id="scheduleInterviewModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Schedule Interview</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="{{ route('recruiter.applications.schedule-interview', $application) }}">
                @csrf
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="interview_date" class="form-label">Interview Date</label>
                            <input type="date" class="form-control" id="interview_date" name="interview_date" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="interview_time" class="form-label">Interview Time</label>
                            <input type="time" class="form-control" id="interview_time" name="interview_time" required>
                        </div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="interview_type" class="form-label">Interview Type</label>
                        <select class="form-select" id="interview_type" name="interview_type" required onchange="toggleInterviewFields()">
                            <option value="online">Online</option>
                            <option value="offline">Offline</option>
                            <option value="phone">Phone</option>
                        </select>
                    </div>
                    
                    <div class="mb-3" id="meeting_link_field">
                        <label for="meeting_link" class="form-label">Meeting Link</label>
                        <input type="url" class="form-control" id="meeting_link" name="meeting_link" 
                               placeholder="https://zoom.us/j/...">
                    </div>
                    
                    <div class="mb-3" id="location_field" style="display: none;">
                        <label for="location" class="form-label">Location</label>
                        <input type="text" class="form-control" id="location" name="location" 
                               placeholder="Office address or meeting room">
                    </div>
                    
                    <div class="mb-3">
                        <label for="interview_notes" class="form-label">Notes (Optional)</label>
                        <textarea class="form-control" id="interview_notes" name="notes" rows="3"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-success">Schedule Interview</button>
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
function toggleInterviewFields() {
    const type = document.getElementById('interview_type').value;
    const meetingField = document.getElementById('meeting_link_field');
    const locationField = document.getElementById('location_field');
    
    if (type === 'online') {
        meetingField.style.display = 'block';
        locationField.style.display = 'none';
        document.getElementById('meeting_link').required = true;
        document.getElementById('location').required = false;
    } else if (type === 'offline') {
        meetingField.style.display = 'none';
        locationField.style.display = 'block';
        document.getElementById('meeting_link').required = false;
        document.getElementById('location').required = true;
    } else {
        meetingField.style.display = 'none';
        locationField.style.display = 'none';
        document.getElementById('meeting_link').required = false;
        document.getElementById('location').required = false;
    }
}

// Set minimum date to tomorrow
document.getElementById('interview_date').min = new Date(Date.now() + 86400000).toISOString().split('T')[0];
</script>
@endsection