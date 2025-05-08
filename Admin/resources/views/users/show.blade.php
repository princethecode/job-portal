@extends('layouts.app')

@section('title', 'User Details')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>User Details</h2>
        <a href="{{ route('admin.users.index') }}" class="btn btn-secondary">
            <i class="fas fa-arrow-left me-1"></i> Back to Users
        </a>
    </div>
    
    <div class="row">
        <div class="col-md-4">
            <!-- User Profile Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex justify-content-between align-items-center">
                    <h6 class="m-0 font-weight-bold text-primary">Profile Information</h6>
                    <div>
                        @if($user['is_active'])
                            <span class="badge bg-success">Active</span>
                        @else
                            <span class="badge bg-danger">Inactive</span>
                        @endif
                    </div>
                </div>
                <div class="card-body">
                    <div class="text-center mb-4">
                        <i class="fas fa-user-circle fa-6x text-gray-300 mb-3"></i>
                        <h4>{{ $user['name'] }}</h4>
                    </div>
                    
                    <div class="mb-3">
                        <p><i class="fas fa-envelope me-2"></i>{{ $user['email'] }}</p>
                        @if($user['mobile'])
                            <p><i class="fas fa-phone me-2"></i>{{ $user['mobile'] }}</p>
                        @endif
                        <p><i class="fas fa-calendar-alt me-2"></i>Joined: {{ \Carbon\Carbon::parse($user['created_at'])->format('M d, Y') }}</p>
                    </div>
                    
                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-{{ $user['is_active'] ? 'warning' : 'success' }}" data-bs-toggle="modal" data-bs-target="#statusModal">
                            <i class="fas fa-{{ $user['is_active'] ? 'ban' : 'check' }} me-1"></i> {{ $user['is_active'] ? 'Deactivate User' : 'Activate User' }}
                        </button>
                        <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#deleteModal">
                            <i class="fas fa-trash me-1"></i> Delete User
                        </button>
                    </div>
                    
                    <!-- Status Modal -->
                    <div class="modal fade" id="statusModal" tabindex="-1" aria-labelledby="statusModalLabel" aria-hidden="true">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title" id="statusModalLabel">Confirm Status Change</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                    Are you sure you want to {{ $user['is_active'] ? 'deactivate' : 'activate' }} the user "{{ $user['name'] }}"?
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                    <form action="{{ route('admin.users.update-status', $user['id']) }}" method="POST">
                                        @csrf
                                        @method('PUT')
                                        <input type="hidden" name="is_active" value="{{ $user['is_active'] ? '0' : '1' }}">
                                        <button type="submit" class="btn btn-{{ $user['is_active'] ? 'warning' : 'success' }}">
                                            {{ $user['is_active'] ? 'Deactivate' : 'Activate' }}
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Delete Modal -->
                    <div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title" id="deleteModalLabel">Confirm Delete</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                    Are you sure you want to delete the user "{{ $user['name'] }}"? This action cannot be undone.
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                    <form action="{{ route('admin.users.destroy', $user['id']) }}" method="POST">
                                        @csrf
                                        @method('DELETE')
                                        <button type="submit" class="btn btn-danger">Delete</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            @if($user['resume_path'])
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Resume</h6>
                </div>
                <div class="card-body">
                    <div class="d-grid gap-2">
                        <a href="{{ env('API_URL') . '/storage/' . $user['resume_path'] }}" target="_blank" class="btn btn-outline-primary">
                            <i class="fas fa-file-pdf me-1"></i> View Resume
                        </a>
                        <a href="{{ env('API_URL') . '/storage/' . $user['resume_path'] }}" download class="btn btn-outline-secondary">
                            <i class="fas fa-download me-1"></i> Download Resume
                        </a>
                    </div>
                </div>
            </div>
            @endif
        </div>
        
        <div class="col-md-8">
            <!-- Skills & Experience Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Skills & Experience</h6>
                </div>
                <div class="card-body">
                    @if($user['skills'])
                    <div class="mb-4">
                        <h5>Skills</h5>
                        <div class="p-3 bg-light rounded">
                            {!! nl2br(e($user['skills'])) !!}
                        </div>
                    </div>
                    @endif
                    
                    @if($user['experience'])
                    <div>
                        <h5>Experience</h5>
                        <div class="p-3 bg-light rounded">
                            {!! nl2br(e($user['experience'])) !!}
                        </div>
                    </div>
                    @endif
                    
                    @if(!$user['skills'] && !$user['experience'])
                    <div class="text-center py-3">
                        <p class="text-muted">No skills or experience information provided</p>
                    </div>
                    @endif
                </div>
            </div>
            
            <!-- Applications Card -->
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Job Applications</h6>
                </div>
                <div class="card-body">
                    @if(isset($applications['data']) && count($applications['data']) > 0)
                    <div class="table-responsive">
                        <table class="table table-bordered" width="100%" cellspacing="0">
                            <thead>
                                <tr>
                                    <th>Job Title</th>
                                    <th>Company</th>
                                    <th>Applied Date</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                @foreach($applications['data'] as $application)
                                <tr>
                                    <td>{{ $application['job']['title'] }}</td>
                                    <td>{{ $application['job']['company'] }}</td>
                                    <td>{{ \Carbon\Carbon::parse($application['applied_date'])->format('M d, Y') }}</td>
                                    <td>
                                        @if($application['status'] == 'Applied')
                                            <span class="badge bg-primary">Applied</span>
                                        @elseif($application['status'] == 'Under Review')
                                            <span class="badge bg-info">Under Review</span>
                                        @elseif($application['status'] == 'Shortlisted')
                                            <span class="badge bg-success">Shortlisted</span>
                                        @elseif($application['status'] == 'Rejected')
                                            <span class="badge bg-danger">Rejected</span>
                                        @endif
                                    </td>
                                    <td>
                                        <a href="{{ route('admin.applications.show', $application['id']) }}" class="btn btn-sm btn-info">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                    </td>
                                </tr>
                                @endforeach
                            </tbody>
                        </table>
                    </div>
                    @else
                    <div class="text-center py-3">
                        <p class="text-muted">No job applications found</p>
                    </div>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
