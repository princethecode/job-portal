@extends('layouts.app')

@section('title', 'Bulk Job Import')

@section('content')
<div class="container-fluid">
    <div class="card shadow mb-4">
        <div class="card-header py-3 d-flex justify-content-between align-items-center">
            <h6 class="m-0 font-weight-bold text-primary">Bulk Job Import</h6>
            <div>
                <a href="{{ route('admin.jobs.index') }}" class="btn btn-sm btn-primary">
                    <i class="fas fa-arrow-left"></i> Back to Jobs
                </a>
                <a href="{{ route('admin.jobs.downloadTemplate') }}" class="btn btn-sm btn-success">
                    <i class="fas fa-download"></i> Download CSV Template
                </a>
            </div>
        </div>
        <div class="card-body">
            @if(session('success'))
                <div class="alert alert-success">
                    {{ session('success') }}
                </div>
            @endif

            @if(session('error'))
                <div class="alert alert-danger">
                    {{ session('error') }}
                </div>
            @endif

            @if ($errors->any())
                <div class="alert alert-danger">
                    <ul class="mb-0">
                        @foreach ($errors->all() as $error)
                            <li>{{ $error }}</li>
                        @endforeach
                    </ul>
                </div>
            @endif

            <div class="row">
                <div class="col-md-6">
                    <div class="card mb-4">
                        <div class="card-header py-3">
                            <h6 class="m-0 font-weight-bold text-primary">Upload CSV File</h6>
                        </div>
                        <div class="card-body">
                            <form action="{{ route('admin.jobs.processImport') }}" method="POST" enctype="multipart/form-data">
                                @csrf
                                <div class="form-group">
                                    <label for="csv_file">Select CSV File</label>
                                    <input type="file" class="form-control-file" id="csv_file" name="csv_file" required accept=".csv">
                                    <small class="form-text text-muted">File must be in CSV format with the correct headers.</small>
                                </div>
                                <button type="submit" class="btn btn-primary">Import Jobs</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header py-3">
                            <h6 class="m-0 font-weight-bold text-primary">Instructions</h6>
                        </div>
                        <div class="card-body">
                            <h5>CSV Format Requirements:</h5>
                            <ol>
                                <li>Download the CSV template using the button above</li>
                                <li>Do not modify the header row or column order</li>
                                <li>All fields marked with (*) are required</li>
                                <li>For job_type, use only: Full-time, Part-time, or Contract</li>
                                <li>Dates should be in YYYY-MM-DD format</li>
                                <li>Save your file with CSV encoding</li>
                            </ol>
                            <h5>Sample Data Format:</h5>
                            <div class="table-responsive">
                                <table class="table table-bordered table-sm">
                                    <thead>
                                        <tr>
                                            <th>title*</th>
                                            <th>company*</th>
                                            <th>job_type*</th>
                                            <th>location*</th>
                                            <th>category*</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td>Software Engineer</td>
                                            <td>Tech Corp</td>
                                            <td>Full-time</td>
                                            <td>New York</td>
                                            <td>IT</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
