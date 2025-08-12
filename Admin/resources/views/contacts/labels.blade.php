@extends('layouts.app')

@section('content')
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-12">
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">Manage Labels</h5>
                    <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#createLabelModal">
                        Create New Label
                    </button>
                </div>

                <div class="card-body">
                    @if (session('success'))
                        <div class="alert alert-success">
                            {{ session('success') }}
                        </div>
                    @endif

                    @if (session('error'))
                        <div class="alert alert-danger">
                            {{ session('error') }}
                        </div>
                    @endif

                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Color</th>
                                    <th>Contacts</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                @foreach($labels as $label)
                                <tr>
                                    <td>
                                        <span class="badge" style="background-color: {{ $label->color }}">
                                            {{ $label->name }}
                                        </span>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <div style="width: 20px; height: 20px; background-color: {{ $label->color }}; border-radius: 3px; margin-right: 8px;"></div>
                                            {{ $label->color }}
                                        </div>
                                    </td>
                                    <td>{{ $label->contacts_count }}</td>
                                    <td>
                                        <button type="button" class="btn btn-sm btn-primary" 
                                                data-bs-toggle="modal" 
                                                data-bs-target="#editLabelModal{{ $label->id }}">
                                            Edit
                                        </button>
                                        @if($label->contacts_count == 0)
                                            <form action="{{ route('labels.destroy', $label) }}" method="POST" class="d-inline">
                                                @csrf
                                                @method('DELETE')
                                                <button type="submit" class="btn btn-sm btn-danger" 
                                                        onclick="return confirm('Are you sure you want to delete this label?')">
                                                    Delete
                                                </button>
                                            </form>
                                        @endif
                                    </td>
                                </tr>

                                <!-- Edit Label Modal -->
                                <div class="modal fade" id="editLabelModal{{ $label->id }}" tabindex="-1">
                                    <div class="modal-dialog">
                                        <div class="modal-content">
                                            <form action="{{ route('labels.update', $label) }}" method="POST">
                                                @csrf
                                                @method('PUT')
                                                <div class="modal-header">
                                                    <h5 class="modal-title">Edit Label</h5>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                </div>
                                                <div class="modal-body">
                                                    <div class="mb-3">
                                                        <label for="name{{ $label->id }}" class="form-label">Name</label>
                                                        <input type="text" class="form-control" id="name{{ $label->id }}" 
                                                               name="name" value="{{ $label->name }}" required>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label for="color{{ $label->id }}" class="form-label">Color</label>
                                                        <input type="color" class="form-control form-control-color" 
                                                               id="color{{ $label->id }}" name="color" 
                                                               value="{{ $label->color }}" required>
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                    <button type="submit" class="btn btn-primary">Save Changes</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                                @endforeach
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Create Label Modal -->
<div class="modal fade" id="createLabelModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="{{ route('labels.store') }}" method="POST">
                @csrf
                <div class="modal-header">
                    <h5 class="modal-title">Create New Label</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="name" class="form-label">Name</label>
                        <input type="text" class="form-control" id="name" name="name" required>
                    </div>
                    <div class="mb-3">
                        <label for="color" class="form-label">Color</label>
                        <input type="color" class="form-control form-control-color" id="color" name="color" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Create Label</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection 