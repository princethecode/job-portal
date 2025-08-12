@extends('layouts.app')

@section('content')
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow">
                <div class="card-header bg-success text-white">
                    <h4 class="mb-0">Password Reset Successful</h4>
                </div>
                <div class="card-body text-center">
                    <div class="mb-4">
                        <i class="fa fa-check-circle text-success" style="font-size: 4rem;"></i>
                    </div>
                    <h5 class="mb-3">Your password has been reset successfully!</h5>
                    <p>You can now log in with your new password.</p>
                                   </div>
            </div>
        </div>
    </div>
</div>
@endsection 