@extends('layouts.app')

@section('title', 'Dashboard')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Dashboard</h2>
        <div class="btn-group">
            <button type="button" class="btn btn-outline-primary" onclick="window.print()">
                <i class="fas fa-print me-1"></i> Print Report
            </button>
        </div>
    </div>

    @if(isset($error))
        <div class="alert alert-danger">
            {{ $error }}
        </div>
    @else
        <!-- Debug Output -->
        <div class="d-none">
            <pre>
Applications By Status: {{ json_encode($applicationsByStatus) }}
Jobs By Type: {{ json_encode($jobsByType) }}
Application Trend: {{ json_encode($applicationTrend) }}
Top Categories: {{ json_encode($topCategories) }}
            </pre>
        </div>
        
        <!-- Statistics Cards -->
        <div class="row mb-4">
            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-primary shadow h-100 py-2 stats-card">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                    Total Users</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">{{ number_format($stats['total_users']) }}</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-users fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-success shadow h-100 py-2 stats-card">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                    Total Jobs</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">{{ number_format($stats['total_jobs']) }}</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-briefcase fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-info shadow h-100 py-2 stats-card">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                    Total Applications</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">{{ number_format($stats['total_applications']) }}</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-file-alt fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-warning shadow h-100 py-2 stats-card">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                    Active Jobs</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">
                                    {{ number_format(App\Models\Job::where('is_active', true)->count()) }}
                                </div>
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
            <!-- Applications Trend Chart -->
            <div class="col-xl-8 col-lg-7">
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Applications Trend (Last 30 Days)</h6>
                    </div>
                    <div class="card-body">
                        <canvas id="applicationTrendChart" style="height: 300px;"></canvas>
                    </div>
                </div>
            </div>

            <!-- Applications by Status Chart -->
            <div class="col-xl-4 col-lg-5">
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Applications by Status</h6>
                    </div>
                    <div class="card-body">
                        <canvas id="applicationStatusChart" style="height: 300px;"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <!-- Second Charts Row -->
        <div class="row mb-4">
            <!-- Jobs by Type Chart -->
            <div class="col-xl-6">
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Jobs by Type</h6>
                    </div>
                    <div class="card-body">
                        <canvas id="jobTypeChart" style="height: 300px;"></canvas>
                    </div>
                </div>
            </div>

            <!-- Top Job Categories Chart -->
            <div class="col-xl-6">
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Top Job Categories</h6>
                    </div>
                    <div class="card-body">
                        <canvas id="categoryChart" style="height: 300px;"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Activities and Applications -->
        <div class="row">
            <!-- Recent Activities -->
            <div class="col-xl-6 mb-4">
                <div class="card shadow">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Recent Activities</h6>
                    </div>
                    <div class="card-body">
                        <div class="timeline">
                            @forelse($recentActivities as $activity)
                                <div class="timeline-item mb-3">
                                    <div class="d-flex">
                                        <div class="flex-shrink-0">
                                            @if($activity['type'] === 'job')
                                                <i class="fas fa-briefcase text-primary"></i>
                                            @else
                                                <i class="fas fa-file-alt text-info"></i>
                                            @endif
                                        </div>
                                        <div class="flex-grow-1 ms-3">
                                            <p class="mb-0">{{ $activity['message'] }}</p>
                                            <small class="text-muted">{{ $activity['date']->diffForHumans() }}</small>
                                        </div>
                                    </div>
                                </div>
                            @empty
                                <p class="text-center text-muted">No recent activities</p>
                            @endforelse
                        </div>
                    </div>
                </div>
            </div>

            <!-- Recent Applications -->
            <div class="col-xl-6 mb-4">
                <div class="card shadow">
                    <div class="card-header py-3 d-flex justify-content-between align-items-center">
                        <h6 class="m-0 font-weight-bold text-primary">Recent Applications</h6>
                        <a href="{{ route('admin.applications.index') }}" class="btn btn-sm btn-primary">
                            View All
                        </a>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-bordered">
                                <thead>
                                    <tr>
                                        <th>Applicant</th>
                                        <th>Job</th>
                                        <th>Status</th>
                                        <th>Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @forelse($stats['recent_applications'] as $application)
                                        <tr>
                                            <td>{{ $application->user ? $application->user->name : 'Deleted User' }}</td>
                                            <td>{{ $application->job ? $application->job->title : 'Deleted Job' }}</td>
                                            <td>
                                                @switch($application->status)
                                                    @case('Applied')
                                                        <span class="badge bg-primary">Applied</span>
                                                        @break
                                                    @case('Under Review')
                                                        <span class="badge bg-info">Under Review</span>
                                                        @break
                                                    @case('Shortlisted')
                                                        <span class="badge bg-success">Shortlisted</span>
                                                        @break
                                                    @case('Rejected')
                                                        <span class="badge bg-danger">Rejected</span>
                                                        @break
                                                    @default
                                                        <span class="badge bg-secondary">{{ $application->status }}</span>
                                                @endswitch
                                            </td>
                                            <td>{{ $application->created_at->format('M d, Y') }}</td>
                                        </tr>
                                    @empty
                                        <tr>
                                            <td colspan="4" class="text-center">No recent applications</td>
                                        </tr>
                                    @endforelse
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    @endif
</div>
@endsection

@push('scripts')
<!-- Ensure Chart.js is loaded -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@3.7.1/dist/chart.min.js"></script>
<script>
// Add console debugging
console.log('Dashboard script loaded');
console.log('Application Status Data:', {!! json_encode($applicationsByStatus) !!});
console.log('Jobs by Type Data:', {!! json_encode($jobsByType) !!});
console.log('Application Trend Data:', {!! json_encode($applicationTrend) !!});
console.log('Top Categories Data:', {!! json_encode($topCategories) !!});

document.addEventListener('DOMContentLoaded', function() {
    try {
        console.log('DOM loaded, initializing charts');
        
        // Check if canvas elements exist
        console.log('Trend chart canvas exists:', !!document.getElementById('applicationTrendChart'));
        console.log('Status chart canvas exists:', !!document.getElementById('applicationStatusChart'));
        console.log('Job type chart canvas exists:', !!document.getElementById('jobTypeChart'));
        console.log('Category chart canvas exists:', !!document.getElementById('categoryChart'));
        
        // Application Trend Chart
        const trendCtx = document.getElementById('applicationTrendChart');
        if (trendCtx) {
            const trendChart = new Chart(trendCtx.getContext('2d'), {
                type: 'line',
                data: {
                    labels: {!! json_encode(collect($applicationTrend)->pluck('date')) !!},
                    datasets: [{
                        label: 'Applications',
                        data: {!! json_encode(collect($applicationTrend)->pluck('count')) !!},
                        borderColor: '#4e73df',
                        backgroundColor: 'rgba(78, 115, 223, 0.05)',
                        tension: 0.3,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            mode: 'index',
                            intersect: false,
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1
                            }
                        }
                    }
                }
            });
            console.log('Trend chart initialized');
        }

        // Application Status Chart
        const statusCtx = document.getElementById('applicationStatusChart');
        if (statusCtx) {
            const statusChart = new Chart(statusCtx.getContext('2d'), {
                type: 'doughnut',
                data: {
                    labels: {!! json_encode(array_keys($applicationsByStatus)) !!},
                    datasets: [{
                        data: {!! json_encode(array_values($applicationsByStatus)) !!},
                        backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b'],
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const label = context.label || '';
                                    const value = context.raw || 0;
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = Math.round((value / total) * 100);
                                    return `${label}: ${value} (${percentage}%)`;
                                }
                            }
                        }
                    }
                }
            });
            console.log('Status chart initialized');
        }

        // Jobs by Type Chart
        const typeCtx = document.getElementById('jobTypeChart');
        if (typeCtx) {
            const typeChart = new Chart(typeCtx.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: {!! json_encode(array_keys($jobsByType)) !!},
                    datasets: [{
                        label: 'Jobs',
                        data: {!! json_encode(array_values($jobsByType)) !!},
                        backgroundColor: '#36b9cc',
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1
                            }
                        }
                    }
                }
            });
            console.log('Type chart initialized');
        }

        // Top Categories Chart
        const categoryCtx = document.getElementById('categoryChart');
        if (categoryCtx) {
            const categoryChart = new Chart(categoryCtx.getContext('2d'), {
                type: 'pie',
                data: {
                    labels: {!! json_encode(array_keys($topCategories)) !!},
                    datasets: [{
                        data: {!! json_encode(array_values($topCategories)) !!},
                        backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b'],
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const label = context.label || '';
                                    const value = context.raw || 0;
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = Math.round((value / total) * 100);
                                    return `${label}: ${value} (${percentage}%)`;
                                }
                            }
                        }
                    }
                }
            });
            console.log('Category chart initialized');
        }
    } catch (error) {
        console.error('Error initializing charts:', error);
    }
});
</script>
@endpush

@push('styles')
<style>
.timeline-item {
    position: relative;
    padding-left: 1.5rem;
    border-left: 2px solid #e3e6f0;
}
.timeline-item:last-child {
    border-left-color: transparent;
}
.timeline-item .fas {
    position: absolute;
    left: -0.5rem;
    width: 1rem;
    height: 1rem;
    text-align: center;
    background: white;
    border-radius: 50%;
}
.stats-card {
    transition: transform 0.3s ease;
}
.stats-card:hover {
    transform: translateY(-5px);
}
@media print {
    .sidebar, .btn-group {
        display: none !important;
    }
    .main-content {
        margin-left: 0 !important;
    }
    .card {
        break-inside: avoid;
    }
}
</style>
@endpush
