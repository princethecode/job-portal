@extends('recruiter.layouts.app')

@section('title', 'Analytics & Reports')
@section('page-title', 'Analytics & Reports')

@section('content')
<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h4>Recruiting Analytics</h4>
        <p class="text-muted">Track your hiring performance and metrics</p>
    </div>
    <div class="btn-group">
        <button type="button" class="btn btn-outline-primary dropdown-toggle" data-bs-toggle="dropdown">
            <i class="fas fa-download me-2"></i>Export Report
        </button>
        <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="{{ route('recruiter.analytics.export', ['format' => 'csv']) }}">
                <i class="fas fa-file-csv me-2"></i>Export as CSV
            </a></li>
            <li><a class="dropdown-item" href="{{ route('recruiter.analytics.export', ['format' => 'pdf']) }}">
                <i class="fas fa-file-pdf me-2"></i>Export as PDF
            </a></li>
        </ul>
    </div>
</div>

<!-- Key Metrics Cards -->
<div class="row mb-4">
    <div class="col-xl-2 col-md-4 col-sm-6 mb-3">
        <div class="card border-left-primary shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Jobs</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['total_jobs'] }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-briefcase fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xl-2 col-md-4 col-sm-6 mb-3">
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

    <div class="col-xl-2 col-md-4 col-sm-6 mb-3">
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

    <div class="col-xl-2 col-md-4 col-sm-6 mb-3">
        <div class="card border-left-warning shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Interviews</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['total_interviews'] }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-calendar-alt fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xl-2 col-md-4 col-sm-6 mb-3">
        <div class="card border-left-success shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Hires</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $metrics['hired_candidates'] }}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-user-check fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-xl-2 col-md-4 col-sm-6 mb-3">
        <div class="card border-left-info shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Avg. Time to Hire</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $avgTimeToHire }} days</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-clock fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Charts Row -->
<div class="row mb-4">
    <!-- Application Funnel -->
    <div class="col-lg-6 mb-4">
        <div class="card shadow">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Application Funnel</h6>
            </div>
            <div class="card-body">
                <canvas id="funnelChart" width="400" height="200"></canvas>
            </div>
        </div>
    </div>

    <!-- Monthly Trends -->
    <div class="col-lg-6 mb-4">
        <div class="card shadow">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Monthly Trends</h6>
            </div>
            <div class="card-body">
                <canvas id="trendsChart" width="400" height="200"></canvas>
            </div>
        </div>
    </div>
</div>

<!-- Performance Tables -->
<div class="row">
    <!-- Top Performing Jobs -->
    <div class="col-lg-8 mb-4">
        <div class="card shadow">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Top Performing Jobs</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered">
                        <thead>
                            <tr>
                                <th>Job Title</th>
                                <th>Applications</th>
                                <th>Views</th>
                                <th>Conversion Rate</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            @forelse($topJobs as $job)
                            <tr>
                                <td>
                                    <a href="{{ route('recruiter.jobs.show', $job) }}">{{ $job->title }}</a>
                                    <br><small class="text-muted">{{ $job->location }}</small>
                                </td>
                                <td>
                                    <span class="badge bg-primary">{{ $job->applications_count }}</span>
                                </td>
                                <td>{{ $job->views_count ?? 0 }}</td>
                                <td>
                                    @php
                                        $conversionRate = $job->views_count > 0 ? round(($job->applications_count / $job->views_count) * 100, 1) : 0;
                                    @endphp
                                    {{ $conversionRate }}%
                                </td>
                                <td>
                                    <span class="badge bg-{{ $job->is_active ? 'success' : 'secondary' }}">
                                        {{ $job->is_active ? 'Active' : 'Inactive' }}
                                    </span>
                                </td>
                            </tr>
                            @empty
                            <tr>
                                <td colspan="5" class="text-center text-muted">No jobs posted yet</td>
                            </tr>
                            @endforelse
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- Key Performance Indicators -->
    <div class="col-lg-4 mb-4">
        <div class="card shadow">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Key Performance Indicators</h6>
            </div>
            <div class="card-body">
                <div class="mb-3">
                    <div class="d-flex justify-content-between">
                        <span>Interview Success Rate</span>
                        <strong>{{ $interviewSuccessRate }}%</strong>
                    </div>
                    <div class="progress mt-1">
                        <div class="progress-bar bg-success" style="width: {{ $interviewSuccessRate }}%"></div>
                    </div>
                </div>

                <div class="mb-3">
                    <div class="d-flex justify-content-between">
                        <span>Application to Interview</span>
                        @php
                            $appToInterview = $metrics['total_applications'] > 0 ? round(($metrics['total_interviews'] / $metrics['total_applications']) * 100, 1) : 0;
                        @endphp
                        <strong>{{ $appToInterview }}%</strong>
                    </div>
                    <div class="progress mt-1">
                        <div class="progress-bar bg-info" style="width: {{ $appToInterview }}%"></div>
                    </div>
                </div>

                <div class="mb-3">
                    <div class="d-flex justify-content-between">
                        <span>Interview to Hire</span>
                        @php
                            $interviewToHire = $metrics['total_interviews'] > 0 ? round(($metrics['hired_candidates'] / $metrics['total_interviews']) * 100, 1) : 0;
                        @endphp
                        <strong>{{ $interviewToHire }}%</strong>
                    </div>
                    <div class="progress mt-1">
                        <div class="progress-bar bg-warning" style="width: {{ $interviewToHire }}%"></div>
                    </div>
                </div>

                <div class="mb-3">
                    <div class="d-flex justify-content-between">
                        <span>Overall Hire Rate</span>
                        @php
                            $overallHireRate = $metrics['total_applications'] > 0 ? round(($metrics['hired_candidates'] / $metrics['total_applications']) * 100, 1) : 0;
                        @endphp
                        <strong>{{ $overallHireRate }}%</strong>
                    </div>
                    <div class="progress mt-1">
                        <div class="progress-bar bg-success" style="width: {{ $overallHireRate }}%"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
// Application Funnel Chart
const funnelCtx = document.getElementById('funnelChart').getContext('2d');
const funnelChart = new Chart(funnelCtx, {
    type: 'bar',
    data: {
        labels: ['Applied', 'Under Review', 'Shortlisted', 'Rejected', 'Hired'],
        datasets: [{
            label: 'Applications',
            data: [
                {{ $applicationFunnel['Applied'] }},
                {{ $applicationFunnel['Under Review'] }},
                {{ $applicationFunnel['Shortlisted'] }},
                {{ $applicationFunnel['Rejected'] }},
                {{ $applicationFunnel['Hired'] }}
            ],
            backgroundColor: [
                '#007bff',
                '#ffc107',
                '#28a745',
                '#dc3545',
                '#17a2b8'
            ]
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});

// Monthly Trends Chart
const trendsCtx = document.getElementById('trendsChart').getContext('2d');
const trendsChart = new Chart(trendsCtx, {
    type: 'line',
    data: {
        labels: [
            @foreach($monthlyData as $month => $data)
                '{{ $month }}',
            @endforeach
        ],
        datasets: [{
            label: 'Jobs Posted',
            data: [
                @foreach($monthlyData as $data)
                    {{ $data['jobs_posted'] }},
                @endforeach
            ],
            borderColor: '#007bff',
            backgroundColor: 'rgba(0, 123, 255, 0.1)',
            tension: 0.1
        }, {
            label: 'Applications',
            data: [
                @foreach($monthlyData as $data)
                    {{ $data['applications_received'] }},
                @endforeach
            ],
            borderColor: '#28a745',
            backgroundColor: 'rgba(40, 167, 69, 0.1)',
            tension: 0.1
        }, {
            label: 'Hires',
            data: [
                @foreach($monthlyData as $data)
                    {{ $data['hires_made'] }},
                @endforeach
            ],
            borderColor: '#ffc107',
            backgroundColor: 'rgba(255, 193, 7, 0.1)',
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