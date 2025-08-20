@extends('layouts.app')

@section('title', 'Recruiter Management')

@section('content')
<div class="container-fluid">
    <!-- Page Header -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 class="h3 mb-0 text-gray-800">
            <i class="fas fa-user-tie me-2"></i>Recruiter Management
        </h1>
        <div class="btn-group">
            <a href="{{ route('admin.recruiters.export') }}" class="btn btn-success btn-sm">
                <i class="fas fa-download me-1"></i>Export CSV
            </a>
        </div>
    </div>

    <!-- Statistics Cards -->
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-primary shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                Total Recruiters
                            </div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $stats['total_recruiters'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-users fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-success shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                Active Recruiters
                            </div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $stats['active_recruiters'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-user-check fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-info shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                Total Jobs Posted
                            </div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $stats['total_jobs_posted'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-briefcase fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-warning shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                Total Applications
                            </div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $stats['total_applications'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-file-alt fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Filters -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Filters & Search</h6>
        </div>
        <div class="card-body">
            <form method="GET" action="{{ route('admin.recruiters.index') }}">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <input type="text" class="form-control" name="search" 
                               placeholder="Search recruiters..." value="{{ request('search') }}">
                    </div>
                    <div class="col-md-2 mb-3">
                        <select class="form-select" name="status">
                            <option value="">All Status</option>
                            <option value="active" {{ request('status') === 'active' ? 'selected' : '' }}>Active</option>
                            <option value="inactive" {{ request('status') === 'inactive' ? 'selected' : '' }}>Inactive</option>
                        </select>
                    </div>
                    <div class="col-md-2 mb-3">
                        <select class="form-select" name="verified">
                            <option value="">All Verification</option>
                            <option value="verified" {{ request('verified') === 'verified' ? 'selected' : '' }}>Verified</option>
                            <option value="unverified" {{ request('verified') === 'unverified' ? 'selected' : '' }}>Unverified</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <select class="form-select" name="industry">
                            <option value="">All Industries</option>
                            @foreach($industries as $industry)
                                <option value="{{ $industry }}" {{ request('industry') === $industry ? 'selected' : '' }}>
                                    {{ $industry }}
                                </option>
                            @endforeach
                        </select>
                    </div>
                    <div class="col-md-2 mb-3">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="fas fa-search me-1"></i>Filter
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Bulk Actions -->
    <div class="card shadow mb-4">
        <div class="card-body">
            <form method="POST" action="{{ route('admin.recruiters.bulk-action') }}" id="bulk-form">
                @csrf
                <div class="row align-items-center">
                    <div class="col-md-3">
                        <label class="form-label">Bulk Actions:</label>
                    </div>
                    <div class="col-md-3">
                        <select class="form-select" name="action" required>
                            <option value="">Select Action</option>
                            <option value="activate">Activate</option>
                            <option value="deactivate">Deactivate</option>
                            <option value="verify">Verify</option>
                            <option value="unverify">Unverify</option>
                            <option value="delete">Delete</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <button type="submit" class="btn btn-warning" onclick="return confirmBulkAction()">
                            <i class="fas fa-cogs me-1"></i>Apply to Selected
                        </button>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">
                            <span id="selected-count">0</span> recruiter(s) selected
                        </small>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Recruiters Table -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Recruiters List</h6>
        </div>
        <div class="card-body">
            @if($recruiters->count() > 0)
                <div class="table-responsive">
                    <table class="table table-bordered" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>
                                    <input type="checkbox" id="select-all" onchange="toggleAll()">
                                </th>
                                <th>Recruiter</th>
                                <th>Company</th>
                                <th>Industry</th>
                                <th>Jobs</th>
                                <th>Applications</th>
                                <th>Status</th>
                                <th>Verified</th>
                                <th>Joined</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($recruiters as $recruiter)
                            <tr>
                                <td>
                                    <input type="checkbox" name="recruiter_ids[]" value="{{ $recruiter->id }}" 
                                           form="bulk-form" class="recruiter-checkbox" onchange="updateSelectedCount()">
                                </td>
                                <td>
                                    <div class="d-flex align-items-center">
                                        <div class="me-3">
                                            @if($recruiter->company_logo)
                                                <img src="{{ asset('storage/' . $recruiter->company_logo) }}" 
                                                     alt="Logo" class="rounded-circle" style="width: 40px; height: 40px;">
                                            @else
                                                <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center" 
                                                     style="width: 40px; height: 40px;">
                                                    <span class="text-white">{{ substr($recruiter->name, 0, 1) }}</span>
                                                </div>
                                            @endif
                                        </div>
                                        <div>
                                            <strong>{{ $recruiter->name }}</strong>
                                            <br>
                                            <small class="text-muted">{{ $recruiter->email }}</small>
                                            @if($recruiter->designation)
                                                <br><small class="text-info">{{ $recruiter->designation }}</small>
                                            @endif
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong>{{ $recruiter->company_name }}</strong>
                                    @if($recruiter->location)
                                        <br><small class="text-muted">{{ $recruiter->location }}</small>
                                    @endif
                                    @if($recruiter->company_size)
                                        <br><small class="text-muted">{{ $recruiter->company_size }} employees</small>
                                    @endif
                                </td>
                                <td>
                                    <span class="badge bg-secondary">{{ $recruiter->industry ?? 'Not specified' }}</span>
                                </td>
                                <td class="text-center">
                                    <span class="badge bg-primary">{{ $recruiter->jobs_count }}</span>
                                </td>
                                <td class="text-center">
                                    <span class="badge bg-info">{{ $recruiter->applications_count }}</span>
                                </td>
                                <td>
                                    <span class="badge bg-{{ $recruiter->is_active ? 'success' : 'danger' }}">
                                        {{ $recruiter->is_active ? 'Active' : 'Inactive' }}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge bg-{{ $recruiter->is_verified ? 'success' : 'warning' }}">
                                        {{ $recruiter->is_verified ? 'Verified' : 'Unverified' }}
                                    </span>
                                </td>
                                <td>
                                    {{ $recruiter->created_at->format('M d, Y') }}
                                    <br>
                                    <small class="text-muted">{{ $recruiter->created_at->diffForHumans() }}</small>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <a href="{{ route('admin.recruiters.show', $recruiter) }}" 
                                           class="btn btn-info btn-sm">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                        
                                        <!-- Status Toggle -->
                                        <form method="POST" action="{{ route('admin.recruiters.update-status', $recruiter) }}" class="d-inline">
                                            @csrf
                                            @method('PUT')
                                            <input type="hidden" name="is_active" value="{{ $recruiter->is_active ? 0 : 1 }}">
                                            <button type="submit" class="btn btn-{{ $recruiter->is_active ? 'warning' : 'success' }} btn-sm"
                                                    onclick="return confirm('Are you sure?')">
                                                <i class="fas fa-{{ $recruiter->is_active ? 'pause' : 'play' }}"></i>
                                            </button>
                                        </form>
                                        
                                        <!-- Delete -->
                                        <form method="POST" action="{{ route('admin.recruiters.destroy', $recruiter) }}" class="d-inline">
                                            @csrf
                                            @method('DELETE')
                                            <button type="submit" class="btn btn-danger btn-sm"
                                                    onclick="return confirm('Are you sure you want to delete this recruiter?')">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <div class="d-flex justify-content-center">
                    {{ $recruiters->links() }}
                </div>
            @else
                <div class="text-center py-4">
                    <i class="fas fa-user-tie fa-3x text-muted mb-3"></i>
                    <h5>No Recruiters Found</h5>
                    <p class="text-muted">No recruiters match your current filters.</p>
                </div>
            @endif
        </div>
    </div>
</div>
@endsection

@section('scripts')
<style>
.border-left-primary {
    border-left: 0.25rem solid #4e73df !important;
}
.border-left-success {
    border-left: 0.25rem solid #1cc88a !important;
}
.border-left-info {
    border-left: 0.25rem solid #36b9cc !important;
}
.border-left-warning {
    border-left: 0.25rem solid #f6c23e !important;
}
</style>

<script>
function toggleAll() {
    const selectAll = document.getElementById('select-all');
    const checkboxes = document.querySelectorAll('.recruiter-checkbox');
    
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAll.checked;
    });
    
    updateSelectedCount();
}

function updateSelectedCount() {
    const checkboxes = document.querySelectorAll('.recruiter-checkbox:checked');
    document.getElementById('selected-count').textContent = checkboxes.length;
}

function confirmBulkAction() {
    const selected = document.querySelectorAll('.recruiter-checkbox:checked');
    const action = document.querySelector('select[name="action"]').value;
    
    if (selected.length === 0) {
        alert('Please select at least one recruiter.');
        return false;
    }
    
    if (!action) {
        alert('Please select an action.');
        return false;
    }
    
    return confirm(`Are you sure you want to ${action} ${selected.length} recruiter(s)?`);
}
</script>
@endsection