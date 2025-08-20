@extends('recruiter.layouts.app')

@section('title', 'Candidates')
@section('page-title', 'Candidate Database')

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h4>Search Candidates</h4>
        <p class="text-muted">Find and connect with potential candidates</p>
    </div>
    <a href="{{ route('recruiter.candidates.saved') }}" class="btn btn-outline-success">
        <i class="fas fa-heart me-2"></i>Saved Candidates
    </a>
</div>

<!-- Search Filters -->
<div class="card mb-4">
    <div class="card-body">
        <form method="GET" action="{{ route('recruiter.candidates.index') }}">
            <div class="row">
                <div class="col-md-4 mb-3">
                    <input type="text" class="form-control" name="search" 
                           placeholder="Search by name, email, skills..." value="{{ request('search') }}">
                </div>
                <div class="col-md-3 mb-3">
                    <input type="text" class="form-control" name="location" 
                           placeholder="Location" value="{{ request('location') }}">
                </div>
                <div class="col-md-3 mb-3">
                    <select class="form-select" name="experience">
                        <option value="">Any Experience</option>
                        <option value="0-1 years" {{ request('experience') === '0-1 years' ? 'selected' : '' }}>0-1 years</option>
                        <option value="1-3 years" {{ request('experience') === '1-3 years' ? 'selected' : '' }}>1-3 years</option>
                        <option value="3-5 years" {{ request('experience') === '3-5 years' ? 'selected' : '' }}>3-5 years</option>
                        <option value="5+ years" {{ request('experience') === '5+ years' ? 'selected' : '' }}>5+ years</option>
                    </select>
                </div>
                <div class="col-md-2 mb-3">
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="fas fa-search me-1"></i>Search
                    </button>
                </div>
            </div>
            
            <div class="row">
                <div class="col-md-3 mb-3">
                    <input type="text" class="form-control" name="company" 
                           placeholder="Current Company" value="{{ request('company') }}">
                </div>
                <div class="col-md-3 mb-3">
                    <input type="number" class="form-control" name="min_salary" 
                           placeholder="Min Expected Salary" value="{{ request('min_salary') }}">
                </div>
                <div class="col-md-3 mb-3">
                    <input type="number" class="form-control" name="max_salary" 
                           placeholder="Max Expected Salary" value="{{ request('max_salary') }}">
                </div>
                <div class="col-md-3 mb-3">
                    <a href="{{ route('recruiter.candidates.index') }}" class="btn btn-outline-secondary w-100">
                        <i class="fas fa-times me-1"></i>Clear
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- Candidates Grid -->
<div class="row">
    @forelse($candidates as $candidate)
    <div class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100 candidate-card">
            <div class="card-body">
                <div class="d-flex align-items-center mb-3">
                    @if($candidate->profile_photo)
                        <img src="{{ asset('storage/' . $candidate->profile_photo) }}" 
                             alt="Profile" class="rounded-circle me-3" style="width: 60px; height: 60px;">
                    @else
                        <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center me-3" 
                             style="width: 60px; height: 60px;">
                            <span class="text-white h5 mb-0">{{ substr($candidate->name, 0, 1) }}</span>
                        </div>
                    @endif
                    <div class="flex-grow-1">
                        <h6 class="mb-1">{{ $candidate->name }}</h6>
                        @if($candidate->job_title)
                            <small class="text-muted">{{ $candidate->job_title }}</small>
                        @endif
                    </div>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary" type="button" data-bs-toggle="dropdown">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu">
                            <li>
                                <a class="dropdown-item" href="{{ route('recruiter.candidates.show', $candidate) }}">
                                    <i class="fas fa-eye me-2"></i>View Profile
                                </a>
                            </li>
                            <li>
                                <form method="POST" action="{{ route('recruiter.candidates.toggle-save', $candidate) }}">
                                    @csrf
                                    <button type="submit" class="dropdown-item">
                                        <i class="fas fa-{{ in_array($candidate->id, $savedCandidateIds) ? 'heart-broken' : 'heart' }} me-2"></i>
                                        {{ in_array($candidate->id, $savedCandidateIds) ? 'Remove from Saved' : 'Save Candidate' }}
                                    </button>
                                </form>
                            </li>
                            @if($candidate->resume_path)
                            <li>
                                <a class="dropdown-item" href="{{ route('recruiter.candidates.download-resume', $candidate) }}">
                                    <i class="fas fa-download me-2"></i>Download Resume
                                </a>
                            </li>
                            @endif
                        </ul>
                    </div>
                </div>
                
                @if($candidate->current_company)
                <p class="text-muted mb-2">
                    <i class="fas fa-building me-1"></i>{{ $candidate->current_company }}
                </p>
                @endif
                
                @if($candidate->location)
                <p class="text-muted mb-2">
                    <i class="fas fa-map-marker-alt me-1"></i>{{ $candidate->location }}
                </p>
                @endif
                
                @if($candidate->experience)
                <p class="text-muted mb-2">
                    <i class="fas fa-briefcase me-1"></i>{{ $candidate->experience }} experience
                </p>
                @endif
                
                @if($candidate->expected_salary)
                <p class="text-muted mb-2">
                    <i class="fas fa-dollar-sign me-1"></i>Expected: ${{ number_format($candidate->expected_salary) }}
                </p>
                @endif
                
                @if($candidate->skills)
                <div class="mb-3">
                    <small class="text-muted">Skills:</small>
                    <p class="small">{{ Str::limit($candidate->skills, 80) }}</p>
                </div>
                @endif
                
                @if($candidate->about_me)
                <div class="mb-3">
                    <small class="text-muted">About:</small>
                    <p class="small">{{ Str::limit($candidate->about_me, 100) }}</p>
                </div>
                @endif
                
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">
                        @if(in_array($candidate->id, $savedCandidateIds))
                            <i class="fas fa-heart text-danger me-1"></i>Saved
                        @endif
                    </small>
                    <div class="btn-group">
                        <a href="{{ route('recruiter.candidates.show', $candidate) }}" class="btn btn-sm btn-outline-primary">
                            View Profile
                        </a>
                        <button type="button" class="btn btn-sm btn-outline-success" 
                                onclick="showInviteModal({{ $candidate->id }}, '{{ $candidate->name }}')">
                            Invite
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    @empty
    <div class="col-12">
        <div class="text-center py-5">
            <i class="fas fa-users fa-3x text-muted mb-3"></i>
            <h4>No Candidates Found</h4>
            <p class="text-muted">Try adjusting your search filters to find more candidates.</p>
        </div>
    </div>
    @endforelse
</div>

<!-- Pagination -->
@if($candidates->hasPages())
<div class="d-flex justify-content-center">
    {{ $candidates->links() }}
</div>
@endif

<!-- Invite to Job Modal -->
<div class="modal fade" id="inviteModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Invite Candidate to Job</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="invite-form" method="POST">
                @csrf
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Candidate: <span id="candidate-name"></span></label>
                    </div>
                    
                    <div class="mb-3">
                        <label for="job_id" class="form-label">Select Job</label>
                        <select class="form-select" id="job_id" name="job_id" required>
                            <option value="">Choose a job...</option>
                            <!-- Jobs will be loaded here -->
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label for="message" class="form-label">Personal Message (Optional)</label>
                        <textarea class="form-control" id="message" name="message" rows="4" 
                                  placeholder="Add a personal message to your invitation..."></textarea>
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
<style>
.candidate-card {
    transition: transform 0.2s;
}

.candidate-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
</style>

<script>
function showInviteModal(candidateId, candidateName) {
    document.getElementById('candidate-name').textContent = candidateName;
    document.getElementById('invite-form').action = `/recruiter/candidates/${candidateId}/invite-to-job`;
    
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