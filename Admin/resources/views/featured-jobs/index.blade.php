@extends('layouts.app')

@section('content')
<div class="container">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Featured Jobs</h2>
        <a href="{{ route('featured-jobs.create') }}" class="btn btn-primary">
            <i class="fas fa-plus"></i> Add New Featured Job
        </a>
    </div>

    @if(session('success'))
        <div class="alert alert-success">
            {{ session('success') }}
        </div>
    @endif

    <div class="card">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Company Logo</th>
                            <th>Job Title</th>
                            <th>Company</th>
                            <th>Location</th>
                            <th>Salary</th>
                            <th>Type</th>
                            <th>Posted Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        @foreach($jobs as $job)
                            <tr>
                                <td>
                                    @if($job->company_logo)
                                        <img src="{{ asset('' . $job->company_logo) }}" alt="{{ $job->company_name }}" class="img-thumbnail" style="max-width: 50px;">
                                    @else
                                        <i class="fas fa-building fa-2x text-muted"></i>
                                    @endif
                                </td>
                                <td>{{ $job->job_title }}</td>
                                <td>{{ $job->company_name }}</td>
                                <td>{{ $job->location }}</td>
                                <td>{{ $job->salary }}</td>
                                <td>{{ $job->job_type }}</td>
                                <td>{{ $job->posted_date->format('M d, Y') }}</td>
                                <td>
                                    <span class="badge {{ $job->is_active ? 'bg-success' : 'bg-danger' }}">
                                        {{ $job->is_active ? 'Active' : 'Inactive' }}
                                    </span>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <a href="{{ route('featured-jobs.edit', $job) }}" class="btn btn-sm btn-info">
                                            <i class="fas fa-edit"></i>
                                        </a>
                                        <form action="{{ route('featured-jobs.destroy', $job) }}" method="POST" class="d-inline">
                                            @csrf
                                            @method('DELETE')
                                            <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Are you sure you want to delete this job?')">
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
            
            <div class="mt-4">
                {{ $jobs->links() }}
            </div>
        </div>
    </div>
</div>
@endsection 