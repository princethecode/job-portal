<?php

return [
    /*
    |--------------------------------------------------------------------------
    | Google OAuth Configuration
    |--------------------------------------------------------------------------
    |
    | Configuration for Google OAuth integration
    |
    */

    'client_id' => env('GOOGLE_CLIENT_ID', '838981324108-pu5ktscdqbpmq3d3kiprm1jb204uup9i.apps.googleusercontent.com'),
    'client_secret' => env('GOOGLE_CLIENT_SECRET'),
    'redirect_uri' => env('GOOGLE_REDIRECT_URI'),
    
    /*
    |--------------------------------------------------------------------------
    | Google API Settings
    |--------------------------------------------------------------------------
    */
    
    'scopes' => [
        'openid',
        'email',
        'profile'
    ],
    
    /*
    |--------------------------------------------------------------------------
    | Token Verification
    |--------------------------------------------------------------------------
    |
    | Whether to verify Google ID tokens on the backend
    |
    */
    
    'verify_tokens' => env('GOOGLE_VERIFY_TOKENS', false),
];