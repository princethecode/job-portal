@extends('recruiter.layouts.app')

@section('title', 'Saved Candidates')
@section('page-title', 'Saved Candidates')

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h4>Saved Candidates</h4>
        <p class="text-muted">Manage your saved candidate list</p>
    </div>
    <a href="{{ route('recruiter.candidates.index') }}" class="btn btn-outline-primary">
        <i class="fas fa-search me-2"></i>Search More Candidates
    </a>
</div>

<!-- Search within saved candidates -->
<div class="card mb-4">
    <div class="card-body">
        <form method="GET" action="{{ route('recruiter.candidates.saved') }}">
            <div class="row">
                <div class="col-md-8">
                    <input type="text" class="form-control" name="search" 
                           placeholder="Search within saved candidates..." value="{{ request('search') }}">
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-outline-primary w-100">
                        <i class="fas fa-search me-1"></i>Search
                    </button>
                </div>
                <div class="col-md-2">
                    <a href="{{ route('recruiter.candidates.saved') }}" class="btn btn-outline-secondary w-100">
                        <i class="fas fa-times me-1"></i>Clear
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- Saved Candidates List -->
<div class="row">
    @forelse($savedCandidates as $candidate)
    <div class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100 saved-candidate-card">
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
                    <div class="text-end">
                        <i class="fas fa-heart text-danger"></i>
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
                
                <!-- Notes -->
                @if($candidate->pivot->notes)
                <div class="mb-3">
                    <small class="text-muted">My Notes:</small>
                    <p class="small bg-light p-2 rounded">{{ Str::limit($candidate->pivot->notes, 100) }}</p>
                </div>
                @endif
                
                <!-- Saved Date -->
                <div class="mb-3">
                    <small class="text-muted">
                        <i class="fas fa-calendar me-1"></i>Saved {{ $candidate->pivot->created_at->diffForHumans() }}
                    </small>
                </div>
                
                <div class="d-flex justify-content-between">
                    <div class="btn-group">
                        <a href="{{ route('recruiter.candidates.show', $candidate) }}" class="btn btn-sm btn-outline-primary">
                            <i class="fas fa-eye me-1"></i>View
                        </a>
                        <button type="button" class="btn btn-sm btn-outline-info" 
                                onclick="showNotesModal({{ $candidate->id }}, '{{ $candidate->name }}', '{{ addslashes($candidate->pivot->notes ?? '') }}')">
                            <i class="fas fa-sticky-note me-1"></i>Notes
                        </button>
                    </div>
                    <div class="btn-group">
                        <button type="button" class="btn btn-sm btn-outline-success" 
                                onclick="showInviteModal({{ $candidate->id }}, '{{ $candidate->name }}')">
                            <i class="fas fa-envelope me-1"></i>Invite
                        </button>
                        <form method="POST" action="{{ route('recruiter.candidates.toggle-save', $candidate) }}" class="d-inline">
                            @csrf
                            <button type="submit" class="btn btn-sm btn-outline-danger" 
                                    onclick="return confirm('Remove this candidate from saved list?')">
                                <i class="fas fa-heart-broken me-1"></i>Remove
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    @empty
    <div class="col-12">
        <div class="text-center py-5">
            <i class="fas fa-heart fa-3x text-muted mb-3"></i>
            <h4>No Saved Candidates</h4>
            <p class="text-muted">You haven't saved any candidates yet. Start by searching and saving candidates you're interested in.</p>
            <a href="{{ route('recruiter.candidates.index') }}" class="btn btn-primary">
                <i class="fas fa-search me-2"></i>Search Candidates
            </a>
        </div>
    </div>
    @endforelse
</div>

<!-- Pagination -->
@if($savedCandidates->hasPages())
<div class="d-flex justify-content-center">
    {{ $savedCandidates->links() }}
</div>
@endif

<!-- Notes Modal -->
<div class="modal fade" id="notesModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Notes for <span id="notes-candidate-name"></span></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="notes-form" method="POST">
                @csrf
                @method('PUT')
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="notes" class="form-label">Your Notes</label>
                        <textarea class="form-control" id="notes" name="notes" rows="6" 
                                  placeholder="Add your notes about this candidate..."></textarea>
                        <small class="text-muted">Keep track of your thoughts, interview feedback, or any other relevant information.</small>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Notes</button>
                </div>
            </form>
        </div>
    </div>
</div>

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
                        <label class="form-label">Candidate: <span id="invite-candidate-name"></span></label>
                    </div>
                    
                    <div class="mb-3">
                        <label for="job_id" class="form-label">Select Job</label>
                        <select class="form-select" id="job_id" name="job_id" required>
                            <option value="">Choose a job...</option>
                            <!-- Jobs will be loaded here -->
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label for="message" class="form-label">Personal Message</label>
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
.saved-candidate-card {
    transition: transform 0.2s;
    border-left: 4px solid #dc3545;
}

.saved-candidate-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
</style>

<script>
function showNotesModal(candidateId, candidateName, currentNotes) {
    document.getElementById('notes-candidate-name').textContent = candidateName;
    document.getElementById('notes').value = currentNotes;
    document.getElementById('notes-form').action = `/recruiter/candidates/${candidateId}/notes`;
    
    new bootstrap.Modal(document.getElementById('notesModal')).show();
}

function showInviteModal(candidateId, candidateName) {
    document.getElementById('invite-candidate-name').textContent = candidateName;
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