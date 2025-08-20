@extends('recruiter.layouts.app')

@section('title', $candidate->name . ' - Profile')
@section('page-title', 'Candidate Profile')

@section('content')
<div class="row">
    <div class="col-lg-8">
        <!-- Candidate Profile -->
        <div class="card shadow mb-4">
            <div class="card-body">
                <div class="row">
                    <div class="col-md-3 text-center">
                        @if($candidate->profile_photo)
                            <img src="{{ asset('storage/' . $candidate->profile_photo) }}" 
                                 alt="Profile" class="rounded-circle mb-3" style="width: 150px; height: 150px;">
                        @else
                            <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center mb-3 mx-auto" 
                                 style="width: 150px; height: 150px;">
                                <span class="text-white h1">{{ substr($candidate->name, 0, 1) }}</span>
                            </div>
                        @endif
                        
                        <div class="d-grid gap-2">
                            <form method="POST" action="{{ route('recruiter.candidates.toggle-save', $candidate) }}">
                                @csrf
                                <button type="submit" class="btn btn-{{ $isSaved ? 'danger' : 'outline-success' }} w-100">
                                    <i class="fas fa-{{ $isSaved ? 'heart-broken' : 'heart' }} me-2"></i>
                                    {{ $isSaved ? 'Remove from Saved' : 'Save Candidate' }}
                                </button>
                            </form>
                            
                            @if($candidate->resume_path)
                            <a href="{{ route('recruiter.candidates.download-resume', $candidate) }}" class="btn btn-outline-primary">
                                <i class="fas fa-download me-2"></i>Download Resume
                            </a>
                            @endif
                            
                            <button type="button" class="btn btn-success" onclick="showInviteModal()">
                                <i class="fas fa-envelope me-2"></i>Invite to Job
                            </button>
                        </div>
                    </div>
                    
                    <div class="col-md-9">
                        <h3>{{ $candidate->name }}</h3>
                        
                        @if($candidate->job_title)
                        <h5 class="text-muted mb-3">{{ $candidate->job_title }}</h5>
                        @endif
                        
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <p class="mb-2">
                                    <i class="fas fa-envelope text-muted me-2"></i>
                                    <a href="mailto:{{ $candidate->email }}">{{ $candidate->email }}</a>
                                </p>
                                @if($candidate->mobile)
                                <p class="mb-2">
                                    <i class="fas fa-phone text-muted me-2"></i>
                                    <a href="tel:{{ $candidate->mobile }}">{{ $candidate->mobile }}</a>
                                </p>
                                @endif
                                @if($candidate->location)
                                <p class="mb-2">
                                    <i class="fas fa-map-marker-alt text-muted me-2"></i>
                                    {{ $candidate->location }}
                                </p>
                                @endif
                            </div>
                            <div class="col-md-6">
                                @if($candidate->current_company)
                                <p class="mb-2">
                                    <i class="fas fa-building text-muted me-2"></i>
                                    {{ $candidate->current_company }}
                                </p>
                                @endif
                                @if($candidate->experience)
                                <p class="mb-2">
                                    <i class="fas fa-briefcase text-muted me-2"></i>
                                    {{ $candidate->experience }} experience
                                </p>
                                @endif
                                @if($candidate->expected_salary)
                                <p class="mb-2">
                                    <i class="fas fa-dollar-sign text-muted me-2"></i>
                                    Expected: ${{ number_format($candidate->expected_salary) }}
                                </p>
                                @endif
                            </div>
                        </div>
                        
                        @if($candidate->about_me)
                        <div class="mb-3">
                            <h6>About</h6>
                            <p>{{ $candidate->about_me }}</p>
                        </div>
                        @endif
                        
                        @if($candidate->skills)
                        <div class="mb-3">
                            <h6>Skills</h6>
                            <p>{{ $candidate->skills }}</p>
                        </div>
                        @endif
                    </div>
                </div>
            </div>
        </div>

        <!-- Experience -->
        @if($candidate->experiences->count() > 0)
        <div class="card shadow mb-4">
            <div class="card-header">
                <h5 class="mb-0">Work Experience</h5>
            </div>
            <div class="card-body">
                @foreach($candidate->experiences as $experience)
                <div class="d-flex mb-4">
                    <div class="flex-shrink-0 me-3">
                        <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center" 
                             style="width: 50px; height: 50px;">
                            <i class="fas fa-briefcase text-white"></i>
                        </div>
                    </div>
                    <div class="flex-grow-1">
                        <h6 class="mb-1">{{ $experience->job_title }}</h6>
                        <p class="text-muted mb-1">{{ $experience->company }}</p>
                        <small class="text-muted">
                            {{ $experience->start_date }} - {{ $experience->end_date ?? 'Present' }}
                        </small>
                        @if($experience->description)
                        <p class="mt-2">{{ $experience->description }}</p>
                        @endif
                    </div>
                </div>
                @if(!$loop->last)<hr>@endif
                @endforeach
            </div>
        </div>
        @endif

        <!-- Applications to My Jobs -->
        @if($applicationsToMyJobs->count() > 0)
        <div class="card shadow">
            <div class="card-header">
                <h5 class="mb-0">Applications to My Jobs</h5>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead>
                            <tr>
                                <th>Job</th>
                                <th>Applied Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($applicationsToMyJobs as $application)
                            <tr>
                                <td>
                                    <strong>{{ $application->job->title }}</strong>
                                    <br>
                                    <small class="text-muted">{{ $application->job->location }}</small>
                                </td>
                                <td>{{ $application->created_at->format('M d, Y') }}</td>
                                <td>
                                    <span class="badge bg-{{ 
                                        $application->status === 'applied' ? 'primary' : 
                                        ($application->status === 'under_review' ? 'warning' : 
                                        ($application->status === 'shortlisted' ? 'success' : 
                                        ($application->status === 'hired' ? 'info' : 'secondary'))) 
                                    }}">
                                        {{ ucfirst(str_replace('_', ' ', $application->status)) }}
                                    </span>
                                </td>
                                <td>
                                    <a href="{{ route('recruiter.applications.show', $application) }}" 
                                       class="btn btn-sm btn-outline-primary">
                                        View Application
                                    </a>
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        @endif
    </div>

    <div class="col-lg-4">
        <!-- Quick Stats -->
        <div class="card shadow mb-4">
            <div class="card-header">
                <h6 class="mb-0">Quick Stats</h6>
            </div>
            <div class="card-body">
                <div class="row text-center">
                    <div class="col-6 mb-3">
                        <h4 class="text-primary">{{ $applicationsToMyJobs->count() }}</h4>
                        <small class="text-muted">Applications to My Jobs</small>
                    </div>
                    <div class="col-6 mb-3">
                        <h4 class="text-success">{{ $candidate->applications->count() }}</h4>
                        <small class="text-muted">Total Applications</small>
                    </div>
                </div>
                
                <hr>
                
                <div class="mb-2">
                    <small class="text-muted">Profile Completeness:</small>
                    @php
                        $completeness = 0;
                        $fields = ['name', 'email', 'mobile', 'location', 'job_title', 'about_me', 'skills', 'resume_path'];
                        foreach($fields as $field) {
                            if($candidate->$field) $completeness += 12.5;
                        }
                    @endphp
                    <div class="progress mt-1">
                        <div class="progress-bar" role="progressbar" style="width: {{ $completeness }}%">
                            {{ round($completeness) }}%
                        </div>
                    </div>
                </div>
                
                <div class="mb-2">
                    <small class="text-muted">Member Since:</small>
                    <br>
                    <strong>{{ $candidate->created_at->format('M Y') }}</strong>
                </div>
                
                <div class="mb-2">
                    <small class="text-muted">Last Active:</small>
                    <br>
                    <strong>{{ $candidate->updated_at->diffForHumans() }}</strong>
                </div>
            </div>
        </div>

        <!-- Notes -->
        @if($isSaved)
        <div class="card shadow">
            <div class="card-header">
                <h6 class="mb-0">My Notes</h6>
            </div>
            <div class="card-body">
                <form method="POST" action="{{ route('recruiter.candidates.update-notes', $candidate) }}">
                    @csrf
                    @method('PUT')
                    <div class="mb-3">
                        <textarea class="form-control" name="notes" rows="4" 
                                  placeholder="Add your notes about this candidate...">{{ $candidate->pivot->notes ?? '' }}</textarea>
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm w-100">
                        <i class="fas fa-save me-2"></i>Save Notes
                    </button>
                </form>
            </div>
        </div>
        @endif
    </div>
</div>

<!-- Invite to Job Modal -->
<div class="modal fade" id="inviteModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Invite {{ $candidate->name }} to Job</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="{{ route('recruiter.candidates.invite-to-job', $candidate) }}">
                @csrf
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="job_id" class="form-label">Select Job</label>
                        <select class="form-select" id="job_id" name="job_id" required>
                            <option value="">Choose a job...</option>
                            <!-- Jobs would be loaded here from your recruiter's active jobs -->
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label for="message" class="form-label">Personal Message</label>
                        <textarea class="form-control" id="message" name="message" rows="4" 
                                  placeholder="Hi {{ $candidate->name }}, I came across your profile and think you'd be a great fit for this position..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-success">Send Invitation</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
function showInviteModal() {
    // Load real jobs from API
    const jobSelect = document.getElementById('job_id');
    jobSelect.innerHTML = '<option value="">Loading jobs...</option>';
    
    fetch('/recruiter/api/jobs/active')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                let options = '<option value="">Choose a job...</option>';
                data.jobs.forEach(job => {
                    const salaryText = job.salary ? ` - $${new Intl.NumberFormat().format(job.salary)}` : '';
                    options += `<option value="${job.id}">${job.title} (${job.location})${salaryText}</option>`;
                });
                jobSelect.innerHTML = options;
            } else {
                jobSelect.innerHTML = '<option value="">No active jobs available</option>';
            }
        })
        .catch(error => {
            console.error('Error loading jobs:', error);
            jobSelect.innerHTML = '<option value="">Error loading jobs</option>';
        });
    
    new bootstrap.Modal(document.getElementById('inviteModal')).show();
}
</script>
@endsection