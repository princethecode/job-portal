@extends('recruiter.layouts.app')

@section('title', 'Interviews')
@section('page-title', 'Interview Management')

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h4>Manage Interviews</h4>
        <p class="text-muted">Schedule and track candidate interviews</p>
    </div>
    <a href="{{ route('recruiter.interviews.calendar') }}" class="btn btn-outline-primary">
        <i class="fas fa-calendar-alt me-2"></i>Calendar View
    </a>
</div>

<!-- Filters -->
<div class="card mb-4">
    <div class="card-body">
        <form method="GET" action="{{ route('recruiter.interviews.index') }}">
            <div class="row">
                <div class="col-md-3">
                    <select class="form-select" name="status">
                        <option value="">All Status</option>
                        <option value="scheduled" {{ request('status') === 'scheduled' ? 'selected' : '' }}>Scheduled</option>
                        <option value="completed" {{ request('status') === 'completed' ? 'selected' : '' }}>Completed</option>
                        <option value="cancelled" {{ request('status') === 'cancelled' ? 'selected' : '' }}>Cancelled</option>
                        <option value="rescheduled" {{ request('status') === 'rescheduled' ? 'selected' : '' }}>Rescheduled</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <input type="date" class="form-control" name="date_from" 
                           placeholder="From Date" value="{{ request('date_from') }}">
                </div>
                <div class="col-md-3">
                    <input type="date" class="form-control" name="date_to" 
                           placeholder="To Date" value="{{ request('date_to') }}">
                </div>
                <div class="col-md-3">
                    <select class="form-select" name="interview_type">
                        <option value="">All Types</option>
                        <option value="online" {{ request('interview_type') === 'online' ? 'selected' : '' }}>Online</option>
                        <option value="offline" {{ request('interview_type') === 'offline' ? 'selected' : '' }}>Offline</option>
                        <option value="phone" {{ request('interview_type') === 'phone' ? 'selected' : '' }}>Phone</option>
                    </select>
                </div>
            </div>
            <div class="row mt-3">
                <div class="col-md-12">
                    <button type="submit" class="btn btn-outline-primary">
                        <i class="fas fa-search me-1"></i>Filter
                    </button>
                    <a href="{{ route('recruiter.interviews.index') }}" class="btn btn-outline-secondary ms-2">
                        <i class="fas fa-times me-1"></i>Clear
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>

<!-- Interviews List -->
<div class="row">
    @forelse($interviews as $interview)
    <div class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100 border-start border-{{ 
            $interview->status === 'scheduled' ? 'warning' : 
            ($interview->status === 'completed' ? 'success' : 
            ($interview->status === 'cancelled' ? 'danger' : 'secondary')) 
        }} border-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <h6 class="card-title mb-0">{{ $interview->user->name }}</h6>
                    <span class="badge bg-{{ 
                        $interview->status === 'scheduled' ? 'warning' : 
                        ($interview->status === 'completed' ? 'success' : 
                        ($interview->status === 'cancelled' ? 'danger' : 'secondary')) 
                    }}">
                        {{ ucfirst($interview->status) }}
                    </span>
                </div>
                
                <p class="text-muted mb-2">
                    <i class="fas fa-briefcase me-1"></i>{{ $interview->job->title }}
                </p>
                
                <p class="text-muted mb-2">
                    <i class="fas fa-calendar me-1"></i>{{ $interview->interview_date->format('M d, Y') }}
                </p>
                
                <p class="text-muted mb-2">
                    <i class="fas fa-clock me-1"></i>{{ $interview->interview_time->format('H:i A') }}
                </p>
                
                <p class="text-muted mb-3">
                    <i class="fas fa-{{ 
                        $interview->interview_type === 'online' ? 'video' : 
                        ($interview->interview_type === 'offline' ? 'map-marker-alt' : 'phone') 
                    }} me-1"></i>{{ ucfirst($interview->interview_type) }}
                    @if($interview->interview_type === 'online' && $interview->meeting_link)
                        <br>
                        <a href="{{ $interview->meeting_link }}" target="_blank" class="small text-primary">
                            Join Meeting
                        </a>
                    @elseif($interview->interview_type === 'offline' && $interview->location)
                        <br>
                        <small>{{ $interview->location }}</small>
                    @endif
                </p>
                
                @if($interview->rating && $interview->status === 'completed')
                <div class="mb-3">
                    <small class="text-muted">Rating: </small>
                    @for($i = 1; $i <= 5; $i++)
                        <i class="fas fa-star {{ $i <= $interview->rating ? 'text-warning' : 'text-muted' }}"></i>
                    @endfor
                </div>
                @endif
                
                <div class="btn-group w-100">
                    <a href="{{ route('recruiter.interviews.show', $interview) }}" class="btn btn-sm btn-outline-primary">
                        <i class="fas fa-eye me-1"></i>View
                    </a>
                    @if($interview->status === 'scheduled')
                        <button type="button" class="btn btn-sm btn-outline-success" 
                                onclick="markCompleted({{ $interview->id }})">
                            <i class="fas fa-check me-1"></i>Complete
                        </button>
                    @endif
                </div>
            </div>
        </div>
    </div>
    @empty
    <div class="col-12">
        <div class="text-center py-5">
            <i class="fas fa-calendar-alt fa-3x text-muted mb-3"></i>
            <h4>No Interviews Found</h4>
            <p class="text-muted">No interviews match your current filters.</p>
        </div>
    </div>
    @endforelse
</div>

<!-- Pagination -->
@if($interviews->hasPages())
<div class="d-flex justify-content-center">
    {{ $interviews->links() }}
</div>
@endif

<!-- Complete Interview Modal -->
<div class="modal fade" id="completeInterviewModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Complete Interview</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="complete-form" method="POST">
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
                        <label for="feedback" class="form-label">Feedback</label>
                        <textarea class="form-control" id="feedback" name="feedback" rows="4" 
                                  placeholder="Interview feedback and notes..."></textarea>
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
@endsection

@section('scripts')
<script>
function markCompleted(interviewId) {
    document.getElementById('complete-form').action = `/recruiter/interviews/${interviewId}/status`;
    new bootstrap.Modal(document.getElementById('completeInterviewModal')).show();
}
</script>
@endsection