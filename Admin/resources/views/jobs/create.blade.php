@extends('layouts.app')

@section('title', 'Create New Job')

@section('content')
<div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Create New Job</h2>
        <a href="{{ route('admin.jobs.index') }}" class="btn btn-secondary">
            <i class="fas fa-arrow-left me-1"></i> Back to Jobs
        </a>
    </div>
    
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Job Details</h6>
        </div>
        <div class="card-body">
            @if($errors->any())
                <div class="alert alert-danger">
                    <ul class="mb-0">
                        @foreach($errors->all() as $error)
                            <li>{{ $error }}</li>
                        @endforeach
                    </ul>
                </div>
            @endif

            <form action="{{ route('admin.jobs.store') }}" method="POST" enctype="multipart/form-data">
                @csrf
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="title" class="form-label">Job Title</label>
                        <input type="text" class="form-control" id="title" name="title" value="{{ old('title') }}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="company" class="form-label">Company</label>
                        <input type="text" class="form-control" id="company" name="company" value="{{ old('company') }}" required>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="location" class="form-label">Location</label>
                        <input type="text" class="form-control" id="location" name="location" value="{{ old('location') }}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="salary" class="form-label">Salary (Optional)</label>
                        <input type="text" class="form-control" id="salary" name="salary" value="{{ old('salary') }}">
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label for="job_type" class="form-label">Job Type</label>
                        <select class="form-select" id="job_type" name="job_type" required>
                            <option value="">Select Job Type</option>
                            <option value="Full-time" {{ old('job_type') == 'Full-time' ? 'selected' : '' }}>Full-time</option>
                            <option value="Part-time" {{ old('job_type') == 'Part-time' ? 'selected' : '' }}>Part-time</option>
                            <option value="Contract" {{ old('job_type') == 'Contract' ? 'selected' : '' }}>Contract</option>
                        </select>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label for="category" class="form-label">
                            Category
                            <i class="fas fa-info-circle text-muted ms-1" 
                               data-bs-toggle="tooltip" 
                               title="Choose from predefined categories or create your own custom category"></i>
                        </label>
                        <select class="form-select @error('category') is-invalid @enderror" id="category_select" onchange="toggleCustomCategory()" required>
                            <option value="">Select Category</option>
                            @foreach($categories as $category)
                                <option value="{{ $category->name }}" {{ old('category') === $category->name ? 'selected' : '' }}>
                                    {{ $category->name }}{{ $category->sort_order >= 999 ? ' (Custom)' : '' }}
                                </option>
                            @endforeach
                            <option value="custom" {{ old('category_type') === 'custom' ? 'selected' : '' }}>
                                ➕ Add Custom Category
                            </option>
                        </select>
                        
                        <!-- Hidden input for the actual category value -->
                        <input type="hidden" name="category" id="category_value" value="{{ old('category') }}">
                        
                        <!-- Custom category input (hidden by default) -->
                        <div id="custom_category_input" class="mt-2" style="display: none;">
                            <input type="text" class="form-control" id="custom_category" 
                                   placeholder="Enter custom category name" 
                                   value="{{ old('category_type') === 'custom' ? old('category') : '' }}">
                            <small class="text-muted">Enter a new category name (e.g., "Construction", "Healthcare", "Logistics")</small>
                        </div>
                        
                        @error('category')
                            <div class="invalid-feedback d-block">{{ $message }}</div>
                        @enderror
                    </div>
                    <div class="col-md-4 mb-3">
                        <label for="posting_date" class="form-label">Posting Date</label>
                        <input type="date" class="form-control" id="posting_date" name="posting_date" value="{{ old('posting_date', date('Y-m-d')) }}" required>
                    </div>
                </div>
                
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="expiry_date" class="form-label">Expiry Date</label>
                        <input type="date" class="form-control" id="expiry_date" name="expiry_date" value="{{ old('expiry_date', date('Y-m-d', strtotime('+30 days'))) }}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label for="is_active" class="form-label">Status</label>
                        <select class="form-select" id="is_active" name="is_active">
                            <option value="1" {{ old('is_active', '1') == '1' ? 'selected' : '' }}>Active</option>
                            <option value="0" {{ old('is_active') == '0' ? 'selected' : '' }}>Inactive</option>
                        </select>
                    </div>
                </div>
                
                <div class="mb-3">
                    <label for="description" class="form-label">Job Description</label>
                    <textarea class="form-control" id="description" name="description" rows="6" required>{{ old('description') }}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="image">Job Image</label>
                    <input type="file" name="image" id="image" class="form-control-file" accept="image/*">
                </div>
                
                <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-save me-1"></i> Create Job
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection

@push('scripts')
<script>
function toggleCustomCategory() {
    console.log('toggleCustomCategory called');
    const select = document.getElementById('category_select');
    const customInput = document.getElementById('custom_category_input');
    const categoryValue = document.getElementById('category_value');
    const customCategoryField = document.getElementById('custom_category');
    
    console.log('Elements found:', {
        select: !!select,
        customInput: !!customInput,
        categoryValue: !!categoryValue,
        customCategoryField: !!customCategoryField
    });
    
    console.log('Selected value:', select ? select.value : 'select not found');
    
    if (select && select.value === 'custom') {
        console.log('Showing custom input');
        if (customInput) {
            customInput.style.display = 'block';
            console.log('Custom input display set to block');
        }
        if (categoryValue) categoryValue.value = '';
        if (customCategoryField) {
            customCategoryField.focus();
            customCategoryField.oninput = function() {
                if (categoryValue) categoryValue.value = this.value;
            };
        }
    } else {
        console.log('Hiding custom input');
        if (customInput) {
            customInput.style.display = 'none';
            console.log('Custom input display set to none');
        }
        if (categoryValue && select) categoryValue.value = select.value;
        if (customCategoryField) customCategoryField.value = '';
    }
}

// Initialize category on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded - admin job create');
    const select = document.getElementById('category_select');
    const categoryValue = document.getElementById('category_value');
    
    console.log('Initial elements check:', {
        select: !!select,
        categoryValue: !!categoryValue
    });
    
    // Initialize Bootstrap tooltips if available
    if (typeof bootstrap !== 'undefined') {
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
    
    // Set initial value
    if (select && select.value && select.value !== 'custom') {
        if (categoryValue) categoryValue.value = select.value;
    }
    
    // Check if we need to show custom input on page load (for validation errors)
    if (select && select.value === 'custom') {
        console.log('Custom selected on page load, calling toggle');
        toggleCustomCategory();
    }
    
    console.log('Initialization complete');
});
</script>
@endpush