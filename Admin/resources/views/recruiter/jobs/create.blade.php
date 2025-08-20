@extends('recruiter.layouts.app')

@section('title', 'Post New Job')
@section('page-title', 'Post New Job')

@section('content')
<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card shadow">
            <div class="card-header">
                <h5 class="mb-0">Create Job Posting</h5>
            </div>
            <div class="card-body">
                <form method="POST" action="{{ route('recruiter.jobs.store') }}" enctype="multipart/form-data">
                    @csrf
                    
                    <div class="row">
                        <div class="col-md-8 mb-3">
                            <label for="title" class="form-label">Job Title *</label>
                            <input type="text" class="form-control @error('title') is-invalid @enderror" 
                                   id="title" name="title" value="{{ old('title') }}" required>
                            @error('title')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>
                        <div class="col-md-4 mb-3">
                            <label for="job_type" class="form-label">Job Type *</label>
                            <select class="form-select @error('job_type') is-invalid @enderror" id="job_type" name="job_type" required>
                                <option value="">Select Type</option>
                                <option value="Full-time" {{ old('job_type') === 'Full-time' ? 'selected' : '' }}>Full-time</option>
                                <option value="Part-time" {{ old('job_type') === 'Part-time' ? 'selected' : '' }}>Part-time</option>
                                <option value="Contract" {{ old('job_type') === 'Contract' ? 'selected' : '' }}>Contract</option>
                                <option value="Internship" {{ old('job_type') === 'Internship' ? 'selected' : '' }}>Internship</option>
                            </select>
                            @error('job_type')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="location" class="form-label">Location *</label>
                            <input type="text" class="form-control @error('location') is-invalid @enderror" 
                                   id="location" name="location" value="{{ old('location') }}" required>
                            @error('location')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="category" class="form-label">
                                Category *
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
                                <small class="text-muted">Enter a new category name (e.g., "Blockchain", "Cybersecurity", "Remote Work")</small>
                            </div>
                            
                            @error('category')
                                <div class="invalid-feedback d-block">{{ $message }}</div>
                            @enderror
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="salary" class="form-label">Salary (Optional)</label>
                            <input type="number" class="form-control @error('salary') is-invalid @enderror" 
                                   id="salary" name="salary" value="{{ old('salary') }}" min="0">
                            @error('salary')
                                <div class="invalid-feedback">{{ $message }}</div>
                            @enderror
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="experience_required" class="form-label">Experience Required</label>
                            <select class="form-select" id="experience_required" name="experience_required">
                                <option value="">Select Experience</option>
                                <option value="0-1 years" {{ old('experience_required') === '0-1 years' ? 'selected' : '' }}>0-1 years</option>
                                <option value="1-3 years" {{ old('experience_required') === '1-3 years' ? 'selected' : '' }}>1-3 years</option>
                                <option value="3-5 years" {{ old('experience_required') === '3-5 years' ? 'selected' : '' }}>3-5 years</option>
                                <option value="5+ years" {{ old('experience_required') === '5+ years' ? 'selected' : '' }}>5+ years</option>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="expiry_date" class="form-label">Application Deadline *</label>
                        <input type="date" class="form-control @error('expiry_date') is-invalid @enderror" 
                               id="expiry_date" name="expiry_date" value="{{ old('expiry_date') }}" required>
                        @error('expiry_date')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>

                    <div class="mb-3">
                        <label for="description" class="form-label">Job Description *</label>
                        <textarea class="form-control @error('description') is-invalid @enderror" 
                                  id="description" name="description" rows="5" required>{{ old('description') }}</textarea>
                        @error('description')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>

                    <div class="mb-3">
                        <label for="requirements" class="form-label">Requirements</label>
                        <textarea class="form-control" id="requirements" name="requirements" rows="4">{{ old('requirements') }}</textarea>
                    </div>

                    <div class="mb-3">
                        <label for="benefits" class="form-label">Benefits</label>
                        <textarea class="form-control" id="benefits" name="benefits" rows="3">{{ old('benefits') }}</textarea>
                    </div>

                    <div class="mb-3">
                        <label for="skills_required" class="form-label">Required Skills</label>
                        <input type="text" class="form-control" id="skills_input" 
                               placeholder="Type skill and press Enter or comma">
                        <div id="skills_container" class="mt-2"></div>
                        <div id="skills_hidden_inputs"></div>
                    </div>

                    <div class="mb-3">
                        <label for="image" class="form-label">Job Image (Optional)</label>
                        <input type="file" class="form-control @error('image') is-invalid @enderror" 
                               id="image" name="image" accept="image/*">
                        @error('image')
                            <div class="invalid-feedback">{{ $message }}</div>
                        @enderror
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="{{ route('recruiter.jobs.index') }}" class="btn btn-secondary">
                            <i class="fas fa-arrow-left me-2"></i>Back to Jobs
                        </a>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Post Job
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script>
let skills = [];

document.getElementById('skills_input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter' || e.key === ',') {
        e.preventDefault();
        addSkill();
    }
});

function addSkill() {
    const input = document.getElementById('skills_input');
    const skill = input.value.trim();
    
    if (skill && !skills.includes(skill)) {
        skills.push(skill);
        updateSkillsDisplay();
        input.value = '';
    }
}

function removeSkill(skill) {
    skills = skills.filter(s => s !== skill);
    updateSkillsDisplay();
}

function updateSkillsDisplay() {
    const container = document.getElementById('skills_container');
    const hiddenContainer = document.getElementById('skills_hidden_inputs');
    
    container.innerHTML = skills.map(skill => 
        `<span class="badge bg-primary me-2 mb-2">
            ${skill}
            <button type="button" class="btn-close btn-close-white ms-1" onclick="removeSkill('${skill}')"></button>
        </span>`
    ).join('');
    
    // Create hidden inputs for each skill
    hiddenContainer.innerHTML = skills.map(skill => 
        `<input type="hidden" name="skills_required[]" value="${skill}">`
    ).join('');
}

function toggleCustomCategory() {
    const select = document.getElementById('category_select');
    const customInput = document.getElementById('custom_category_input');
    const categoryValue = document.getElementById('category_value');
    const customCategoryField = document.getElementById('custom_category');
    
    if (select.value === 'custom') {
        customInput.style.display = 'block';
        categoryValue.value = '';
        customCategoryField.focus();
        
        // Listen for input in custom category field
        customCategoryField.oninput = function() {
            categoryValue.value = this.value;
        };
    } else {
        customInput.style.display = 'none';
        categoryValue.value = select.value;
        customCategoryField.value = '';
    }
}

// Initialize category on page load
document.addEventListener('DOMContentLoaded', function() {
    const select = document.getElementById('category_select');
    const categoryValue = document.getElementById('category_value');
    
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Set initial value
    if (select.value && select.value !== 'custom') {
        categoryValue.value = select.value;
    }
    
    // Check if we need to show custom input on page load (for validation errors)
    if (select.value === 'custom') {
        toggleCustomCategory();
    }
});
</script>
@endsection