@extends('layouts.app')

@section('title', 'Recruiter Details - ' . $recruiter->name)

@section('content')
<div class="container-fluid">
    <!-- Page Header -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="{{ route('admin.recruiters.index') }}">Recruiters</a></li>
                    <li class="breadcrumb-item active">{{ $recruiter->name }}</li>
                </ol>
            </nav>
            <h1 class="h3 mb-0 text-gray-800">Recruiter Details</h1>
        </div>
        <div class="btn-group">
            <a href="{{ route('admin.recruiters.index') }}" class="btn btn-secondary btn-sm">
                <i class="fas fa-arrow-left me-1"></i>Back to List
            </a>
        </div>
    </div>

    <div class="row">
        <!-- Recruiter Profile -->
        <div class="col-lg-4 mb-4">
            <div class="card shadow">
                <div class="card-body text-center">
                    @if($recruiter->company_logo)
                        <img src="{{ asset('storage/' . $recruiter->company_logo) }}" 
                             alt="Company Logo" class="rounded-circle mb-3" style="width: 100px; height: 100px;">
                    @else
                        <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center mb-3 mx-auto" 
                             style="width: 100px; height: 100px;">
                            <span class="text-white h2">{{ substr($recruiter->name, 0, 1) }}</span>
                        </div>
                    @endif
                    
                    <h5>{{ $recruiter->name }}</h5>
                    <p class="text-muted">{{ $recruiter->designation ?? 'Recruiter' }}</p>
                    
                    <div class="mb-3">
                        <span class="badge bg-{{ $recruiter->is_active ? 'success' : 'danger' }} me-2">
                            {{ $recruiter->is_active ? 'Active' : 'Inactive' }}
                        </span>
                        <span class="badge bg-{{ $recruiter->is_verified ? 'success' : 'warning' }}">
                            {{ $recruiter->is_verified ? 'Verified' : 'Unverified' }}
                        </span>
                    </div>
                    
                    <!-- Quick Actions -->
                    <div class="d-grid gap-2">
                        <form method="POST" action="{{ route('admin.recruiters.update-status', $recruiter) }}">
                            @csrf
                            @method('PUT')
                            <input type="hidden" name="is_active" value="{{ $recruiter->is_active ? 0 : 1 }}">
                            <button type="submit" class="btn btn-{{ $recruiter->is_active ? 'warning' : 'success' }} w-100">
                                <i class="fas fa-{{ $recruiter->is_active ? 'pause' : 'play' }} me-2"></i>
                                {{ $recruiter->is_active ? 'Deactivate' : 'Activate' }}
                            </button>
                        </form>
                        
                        <form method="POST" action="{{ route('admin.recruiters.update-verification', $recruiter) }}">
                            @csrf
                            @method('PUT')
                            <input type="hidden" name="is_verified" value="{{ $recruiter->is_verified ? 0 : 1 }}">
                            <button type="submit" class="btn btn-{{ $recruiter->is_verified ? 'secondary' : 'info' }} w-100">
                                <i class="fas fa-{{ $recruiter->is_verified ? 'times' : 'check' }} me-2"></i>
                                {{ $recruiter->is_verified ? 'Unverify' : 'Verify' }}
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Contact Information -->
            <div class="card shadow mt-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold text-primary">Contact Information</h6>
                </div>
                <div class="card-body">
                    <div class="mb-2">
                        <strong>Email:</strong>
                        <br><a href="mailto:{{ $recruiter->email }}">{{ $recruiter->email }}</a>
                    </div>
                    @if($recruiter->mobile)
                    <div class="mb-2">
                        <strong>Phone:</strong>
                        <br><a href="tel:{{ $recruiter->mobile }}">{{ $recruiter->mobile }}</a>
                    </div>
                    @endif
                    @if($recruiter->location)
                    <div class="mb-2">
                        <strong>Location:</strong>
                        <br>{{ $recruiter->location }}
                    </div>
                    @endif
                    <div class="mb-2">
                        <strong>Member Since:</strong>
                        <br>{{ $recruiter->created_at->format('M d, Y') }}
                    </div>
                </div>
            </div>
        </div>

        <!-- Main Content -->
        <div class="col-lg-8">
            <!-- Metrics Cards -->
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="card border-left-primary shadow h-100 py-2">
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <div class="col mr-2">
                                    <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Jobs Posted</div>
                                    <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['total_jobs'] }}</div>
                                </div>
                                <div class="col-auto">
                                    <i class="fas fa-briefcase fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-3 mb-3">
                    <div class="card border-left-success shadow h-100 py-2">
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <div class="col mr-2">
                                    <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Active Jobs</div>
                                    <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['active_jobs'] }}</div>
                                </div>
                                <div class="col-auto">
                                    <i class="fas fa-check-circle fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-3 mb-3">
                    <div class="card border-left-info shadow h-100 py-2">
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <div class="col mr-2">
                                    <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Applications</div>
                                    <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['total_applications'] }}</div>
                                </div>
                                <div class="col-auto">
                                    <i class="fas fa-file-alt fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-3 mb-3">
                    <div class="card border-left-warning shadow h-100 py-2">
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <div class="col mr-2">
                                    <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Hires</div>
                                    <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['hired_candidates'] }}</div>
                                </div>
                                <div class="col-auto">
                                    <i class="fas fa-user-check fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Company Information -->
            <div class="card shadow mb-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold text-primary">Company Information</h6>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <strong>Company Name:</strong>
                                <br>{{ $recruiter->company_name }}
                            </div>
                            @if($recruiter->company_website)
                            <div class="mb-3">
                                <strong>Website:</strong>
                                <br><a href="{{ $recruiter->company_website }}" target="_blank">{{ $recruiter->company_website }}</a>
                            </div>
                            @endif
                            @if($recruiter->industry)
                            <div class="mb-3">
                                <strong>Industry:</strong>
                                <br>{{ $recruiter->industry }}
                            </div>
                            @endif
                        </div>
                        <div class="col-md-6">
                            @if($recruiter->company_size)
                            <div class="mb-3">
                                <strong>Company Size:</strong>
                                <br>{{ $recruiter->company_size }} employees
                            </div>
                            @endif
                            @if($recruiter->company_description)
                            <div class="mb-3">
                                <strong>Description:</strong>
                                <br>{{ $recruiter->company_description }}
                            </div>
                            @endif
                            @if($recruiter->company_license)
                            <div class="mb-3">
                                <strong>Company License:</strong>
                                <br>
                                <a href="{{ asset('storage/' . $recruiter->company_license) }}" 
                                   target="_blank" class="btn btn-sm btn-outline-primary">
                                    <i class="fas fa-file-alt me-1"></i>View License
                                </a>
                                @if($recruiter->license_uploaded_at)
                                <br><small class="text-muted">Uploaded: {{ $recruiter->license_uploaded_at->format('M d, Y H:i A') }}</small>
                                @endif
                            </div>
                            @else
                            <div class="mb-3">
                                <strong>Company License:</strong>
                                <br><span class="text-muted">Not uploaded</span>
                            </div>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- Activity Chart -->
            <div class="card shadow mb-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold text-primary">Activity Trends (Last 6 Months)</h6>
                </div>
                <div class="card-body">
                    <canvas id="activityChart" width="400" height="100"></canvas>
                </div>
            </div>

            <!-- Recent Jobs -->
            <div class="card shadow mb-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold text-primary">Recent Jobs</h6>
                </div>
                <div class="card-body">
                    @if($recentJobs->count() > 0)
                        <div class="table-responsive">
                            <table class="table table-bordered">
                                <thead>
                                    <tr>
                                        <th>Job Title</th>
                                        <th>Location</th>
                                        <th>Applications</th>
                                        <th>Status</th>
                                        <th>Posted</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @foreach($recentJobs as $job)
                                    <tr>
                                        <td>
                                            <strong>{{ $job->title }}</strong>
                                            <br><small class="text-muted">{{ $job->job_type }}</small>
                                        </td>
                                        <td>{{ $job->location }}</td>
                                        <td>
                                            <span class="badge bg-primary">{{ $job->applications->count() }}</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-{{ $job->is_active ? 'success' : 'secondary' }}">
                                                {{ $job->is_active ? 'Active' : 'Inactive' }}
                                            </span>
                                        </td>
                                        <td>{{ $job->created_at->format('M d, Y') }}</td>
                                    </tr>
                                    @endforeach
                                </tbody>
                            </table>
                        </div>
                    @else
                        <p class="text-muted text-center">No jobs posted yet.</p>
                    @endif
                </div>
            </div>

            <!-- Recent Applications -->
            <div class="card shadow">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold text-primary">Recent Applications</h6>
                </div>
                <div class="card-body">
                    @if($recentApplications->count() > 0)
                        <div class="table-responsive">
                            <table class="table table-bordered">
                                <thead>
                                    <tr>
                                        <th>Candidate</th>
                                        <th>Job</th>
                                        <th>Status</th>
                                        <th>Applied</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @foreach($recentApplications as $application)
                                    <tr>
                                        <td>
                                            <strong>{{ $application->user->name }}</strong>
                                            <br><small class="text-muted">{{ $application->user->email }}</small>
                                        </td>
                                        <td>{{ $application->job->title }}</td>
                                        <td>
                                            <span class="badge bg-{{ 
                                                $application->status === 'Applied' ? 'primary' : 
                                                ($application->status === 'Under Review' ? 'warning' : 
                                                ($application->status === 'Shortlisted' ? 'success' : 
                                                ($application->status === 'Hired' ? 'info' : 'secondary'))) 
                                            }}">
                                                {{ $application->status }}
                                            </span>
                                        </td>
                                        <td>{{ $application->created_at->format('M d, Y') }}</td>
                                    </tr>
                                    @endforeach
                                </tbody>
                            </table>
                        </div>
                    @else
                        <p class="text-muted text-center">No applications received yet.</p>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
// Activity Chart
const ctx = document.getElementById('activityChart').getContext('2d');
const activityChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: [
            @foreach($monthlyStats as $month => $data)
                '{{ $month }}',
            @endforeach
        ],
        datasets: [{
            label: 'Jobs Posted',
            data: [
                @foreach($monthlyStats as $data)
                    {{ $data['jobs'] }},
                @endforeach
            ],
            borderColor: '#007bff',
            backgroundColor: 'rgba(0, 123, 255, 0.1)',
            tension: 0.1
        }, {
            label: 'Applications Received',
            data: [
                @foreach($monthlyStats as $data)
                    {{ $data['applications'] }},
                @endforeach
            ],
            borderColor: '#28a745',
            backgroundColor: 'rgba(40, 167, 69, 0.1)',
            tension: 0.1
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            }
        },
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});
</script>

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
@endsection