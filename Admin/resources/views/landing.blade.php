<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Download Job Portal App - Find Your Dream Job Today</title>
    <meta name="description" content="Download the Job Portal Android app and discover thousands of job opportunities at your fingertips.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #5B68DF;
            --primary-dark: #4051DB;
            --primary-light: #8C96E9;
            --accent-color: #FF7043;
            --accent-dark: #E56238;
            --text-dark: #1F2937;
            --text-medium: #4B5563;
            --text-light: #6B7280;
            --bg-light: #F9FAFB;
            --bg-white: #FFFFFF;
            --shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --border-radius: 12px;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Poppins', sans-serif;
            line-height: 1.6;
            color: var(--text-dark);
            background-color: var(--bg-light);
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        header {
            background-color: var(--bg-white);
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
            position: sticky;
            top: 0;
            z-index: 100;
        }
        
        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 0;
        }
        
        .logo {
            font-size: 24px;
            font-weight: bold;
            color: var(--primary-color);
            text-decoration: none;
        }
        
        .nav-links {
            display: flex;
            gap: 30px;
        }
        
        .nav-links a {
            text-decoration: none;
            color: var(--text-medium);
            font-weight: 500;
            transition: color 0.3s ease;
        }
        
        .nav-links a:hover {
            color: var(--primary-color);
        }
        
        .hero {
            padding: 80px 0;
            display: flex;
            align-items: center;
            flex-wrap: wrap;
        }
        
        .hero-content {
            flex: 1;
            min-width: 300px;
            padding-right: 40px;
        }
        
        .hero-image {
            flex: 1;
            min-width: 300px;
            text-align: center;
        }
        
        .hero-image img {
            max-width: 100%;
            height: auto;
            border-radius: var(--border-radius);
            box-shadow: var(--shadow);
        }
        
        h1 {
            font-size: 3rem;
            margin-bottom: 20px;
            color: var(--text-dark);
            line-height: 1.2;
        }
        
        h1 span {
            color: var(--primary-color);
        }
        
        p.hero-text {
            font-size: 1.2rem;
            color: var(--text-medium);
            margin-bottom: 30px;
        }
        
        .download-btn {
            display: inline-block;
            background-color: var(--primary-color);
            color: white;
            padding: 16px 32px;
            font-size: 1.1rem;
            font-weight: 600;
            border-radius: 50px;
            text-decoration: none;
            transition: background-color 0.3s ease, transform 0.3s ease;
            cursor: pointer;
            box-shadow: 0 4px 6px rgba(91, 104, 223, 0.3);
        }
        
        .download-btn:hover {
            background-color: var(--primary-dark);
            transform: translateY(-2px);
            box-shadow: 0 6px 10px rgba(91, 104, 223, 0.4);
        }
        
        .download-btn .icon {
            margin-right: 10px;
        }
        
        .features {
            padding: 80px 0;
            background-color: var(--bg-white);
        }
        
        .section-title {
            text-align: center;
            margin-bottom: 60px;
        }
        
        .section-title h2 {
            font-size: 2.5rem;
            color: var(--text-dark);
            margin-bottom: 20px;
        }
        
        .section-title p {
            font-size: 1.1rem;
            color: var(--text-medium);
            max-width: 600px;
            margin: 0 auto;
        }
        
        .feature-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 40px;
        }
        
        .feature-card {
            background: var(--bg-white);
            border-radius: var(--border-radius);
            padding: 30px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        
        .feature-card:hover {
            transform: translateY(-5px);
            box-shadow: var(--shadow);
        }
        
        .feature-icon {
            background-color: var(--primary-light);
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
        }
        
        .feature-icon i {
            font-size: 24px;
            color: var(--primary-color);
        }
        
        .feature-title {
            font-size: 1.3rem;
            margin-bottom: 15px;
            color: var(--text-dark);
        }
        
        .feature-desc {
            color: var(--text-medium);
        }
        
        .how-to-download {
            padding: 80px 0;
        }
        
        .steps {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 40px;
        }
        
        .step {
            flex: 1;
            min-width: 250px;
            max-width: 300px;
            text-align: center;
            position: relative;
        }
        
        .step-number {
            background-color: var(--primary-color);
            color: white;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            margin: 0 auto 20px;
        }
        
        .step-title {
            font-size: 1.2rem;
            margin-bottom: 15px;
            color: var(--text-dark);
        }
        
        .step-desc {
            color: var(--text-medium);
        }
        
        .arrow {
            position: absolute;
            top: 20px;
            right: -20px;
            font-size: 24px;
            color: var(--text-light);
            display: none;
        }
        
        @media (min-width: 768px) {
            .arrow {
                display: block;
            }
            
            .step:last-child .arrow {
                display: none;
            }
        }
        
        .cta {
            padding: 100px 0;
            background-color: var(--primary-color);
            color: white;
            text-align: center;
        }
        
        .cta h2 {
            font-size: 2.5rem;
            margin-bottom: 20px;
        }
        
        .cta p {
            font-size: 1.1rem;
            margin-bottom: 40px;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
        }
        
        .cta-btn {
            display: inline-block;
            background-color: white;
            color: var(--primary-color);
            padding: 16px 32px;
            font-size: 1.1rem;
            font-weight: 600;
            border-radius: 50px;
            text-decoration: none;
            transition: all 0.3s ease;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
        
        .cta-btn:hover {
            background-color: var(--bg-light);
            transform: translateY(-2px);
            box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
        }
        
        .testimonials {
            padding: 80px 0;
            background-color: var(--bg-light);
        }
        
        .testimonial-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 40px;
        }
        
        .testimonial-card {
            background: var(--bg-white);
            border-radius: var(--border-radius);
            padding: 30px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
        }
        
        .testimonial-text {
            font-style: italic;
            margin-bottom: 20px;
            color: var(--text-medium);
        }
        
        .testimonial-author {
            display: flex;
            align-items: center;
        }
        
        .author-avatar {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            overflow: hidden;
            margin-right: 15px;
        }
        
        .author-avatar img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        
        .author-name {
            font-weight: 600;
            color: var(--text-dark);
        }
        
        .author-title {
            font-size: 0.9rem;
            color: var(--text-light);
        }
        
        footer {
            background-color: var(--text-dark);
            color: white;
            padding: 60px 0 20px;
        }
        
        .footer-content {
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            gap: 40px;
            margin-bottom: 40px;
        }
        
        .footer-logo {
            flex: 1;
            min-width: 200px;
        }
        
        .footer-logo a {
            color: white;
            text-decoration: none;
            font-size: 24px;
            font-weight: bold;
        }
        
        .footer-logo p {
            margin-top: 20px;
            color: #D1D5DB;
        }
        
        .footer-links {
            flex: 1;
            min-width: 200px;
        }
        
        .footer-links h3 {
            font-size: 1.2rem;
            margin-bottom: 20px;
            color: white;
        }
        
        .footer-links ul {
            list-style: none;
        }
        
        .footer-links li {
            margin-bottom: 10px;
        }
        
        .footer-links a {
            text-decoration: none;
            color: #D1D5DB;
            transition: color 0.3s ease;
        }
        
        .footer-links a:hover {
            color: white;
        }
        
        .copyright {
            text-align: center;
            padding-top: 20px;
            border-top: 1px solid #374151;
            color: #9CA3AF;
            font-size: 0.9rem;
        }
        
        @media (max-width: 768px) {
            .hero {
                padding: 60px 0;
            }
            
            .hero-content {
                padding-right: 0;
                margin-bottom: 40px;
                text-align: center;
            }
            
            h1 {
                font-size: 2.5rem;
            }
            
            .nav-links {
                display: none;
            }
            
            .section-title h2 {
                font-size: 2rem;
            }
            
            .cta h2 {
                font-size: 2rem;
            }
            
            .download-btn, .cta-btn {
                width: 100%;
                text-align: center;
            }
        }
        
        /* Pulse animation for download button */
        @keyframes pulse {
            0% {
                box-shadow: 0 0 0 0 rgba(91, 104, 223, 0.4);
            }
            70% {
                box-shadow: 0 0 0 10px rgba(91, 104, 223, 0);
            }
            100% {
                box-shadow: 0 0 0 0 rgba(91, 104, 223, 0);
            }
        }
        
        .pulse {
            animation: pulse 2s infinite;
        }
        
        /* App Carousel Styles */
        .app-carousel {
            position: relative;
            width: 300px;
            height: 600px;
            margin: 0 auto;
            overflow: hidden;
            border-radius: 30px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            background-color: #000;
        }
        
        .carousel-item {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            opacity: 0;
            transition: opacity 0.5s ease;
        }
        
        .carousel-item.active {
            opacity: 1;
        }
        
        .screenshot {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        
        .carousel-controls {
            position: absolute;
            bottom: 20px;
            left: 0;
            right: 0;
            display: flex;
            justify-content: center;
            gap: 20px;
        }
        
        .carousel-control {
            width: 40px;
            height: 40px;
            background-color: rgba(255, 255, 255, 0.7);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .carousel-control:hover {
            background-color: var(--primary-color);
            color: white;
        }
        
        @media (max-width: 768px) {
            .app-carousel {
                width: 280px;
                height: 560px;
            }
        }
        
        /* Phone Mockup and Slider Styles */
        .phone-mockup {
            margin: 0 auto;
            max-width: 320px;
            padding: 20px 0;
        }
        
        .phone-frame {
            position: relative;
            width: 280px;
            height: 570px;
            margin: 0 auto;
            border-radius: 36px;
            background-color: #1F2937;
            padding: 10px;
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
            overflow: hidden;
        }
        
        .app-slider {
            position: relative;
            width: 260px;
            height: 550px;
            margin: 0 auto;
            border-radius: 26px;
            overflow: hidden;
            background-color: white;
        }
        
        .slider-track {
            position: relative;
            height: 100%;
            width: 100%;
            overflow: hidden;
        }
        
        .slide {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            opacity: 0;
            transition: opacity 0.6s ease-in-out;
            background-color: #f4f4f4;
        }
        
        .slide.active {
            opacity: 1;
        }
        
        .slide img {
            width: 100%;
            height: 100%;
            object-fit: contain;
            padding: 10px;
            display: block;
            max-height: 100%;
        }
        
        .slider-controls {
            position: absolute;
            bottom: 20px;
            left: 0;
            right: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10;
            padding: 0 15px;
        }
        
        .control-prev, .control-next {
            background-color: rgba(255, 255, 255, 0.8);
            border: none;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s;
            color: var(--primary-color);
        }
        
        .control-prev:hover, .control-next:hover {
            background-color: var(--primary-color);
            color: white;
        }
        
        .slider-dots {
            display: flex;
            gap: 8px;
            margin: 0 15px;
        }
        
        .dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background-color: rgba(255, 255, 255, 0.5);
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .dot.active {
            background-color: var(--primary-color);
            transform: scale(1.2);
        }
        
        @media (max-width: 768px) {
            .phone-frame {
                width: 260px;
                height: 530px;
            }
            
            .app-slider {
                width: 240px;
                height: 510px;
            }
        }
        
        /* Simple Slider Styles */
        .simple-slider {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px 0;
        }
        
        .slider-container {
            position: relative;
            width: 100%;
            height: 450px;
            overflow: hidden;
            margin-bottom: 20px;
            border-radius: 15px;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
            background-color: white;
        }
        
        .slide {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            opacity: 0;
            transition: opacity 0.6s ease-in-out;
            background-color: #f4f4f4;
        }
        
        .slide.active {
            opacity: 1;
        }
        
        .slide img {
            width: 100%;
            height: 100%;
            object-fit: contain;
            padding: 10px;
            display: block;
            max-height: 100%;
        }
        
        .slider-navigation {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            margin-top: 15px;
        }
        
        .nav-btn {
            background-color: var(--primary-color);
            color: white;
            border: none;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
            font-size: 14px;
        }
        
        .nav-btn:hover {
            background-color: var(--primary-dark);
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }
        
        .slider-indicators {
            display: flex;
            gap: 8px;
            margin: 0 15px;
        }
        
        .indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background-color: #ccc;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .indicator.active {
            background-color: var(--primary-color);
            transform: scale(1.2);
        }
        
        @media (max-width: 768px) {
            .slider-container {
                height: 300px;
            }
        }
        
        /* Wind Blade Section Styles */
        .wind-blade-section {
            padding: 80px 0;
            background-color: var(--bg-white);
        }
        
        .wind-blade-content {
            display: flex;
            flex-wrap: wrap;
            gap: 40px;
            margin-top: 40px;
        }
        
        .wind-blade-info {
            flex: 1;
            min-width: 300px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 30px;
        }
        
        .info-card {
            background: var(--bg-light);
            padding: 30px;
            border-radius: var(--border-radius);
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            transition: transform 0.3s ease;
        }
        
        .info-card:hover {
            transform: translateY(-5px);
        }
        
        .info-icon {
            background-color: var(--primary-light);
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
        }
        
        .info-icon i {
            font-size: 24px;
            color: var(--primary-color);
        }
        
        .info-card h3 {
            font-size: 1.3rem;
            margin-bottom: 15px;
            color: var(--text-dark);
        }
        
        .info-card p {
            color: var(--text-medium);
            line-height: 1.6;
        }
        
        .wind-blade-slider {
            flex: 1;
            min-width: 300px;
        }
        
        .wind-blade-slider .slider-container {
            height: 400px;
            border-radius: var(--border-radius);
            overflow: hidden;
            box-shadow: var(--shadow);
        }
        
        .wind-blade-slider .slide img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        
        @media (max-width: 768px) {
            .wind-blade-content {
                flex-direction: column;
            }
            
            .wind-blade-slider .slider-container {
                height: 300px;
            }
        }
    </style>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <header>
        <div class="container">
            <nav class="navbar">
                <a href="#" class="logo">Wind Blade Jobs</a>
                <div class="nav-links">
                    <a href="#features">Features</a>
                    <a href="#how-to">How to Download</a>
                    <a href="#testimonials">Testimonials</a>
                    <a href="jobportal.apk" class="download-btn">
                        <i class="fa-solid fa-download icon"></i>Download for Android
                    </a>
                </div>
            </nav>
        </div>
    </header>
    

    
    <!-- New Wind Blade Section -->
    <section class="wind-blade-section" id="wind-blade">             
                <div class="wind-blade-slider">
                    <div class="slider-container">
                        <div class="slide active">
                            <img src="images/wind-blade-1.jpg" alt="Wind Blade Technology 1">
                        </div>
                        <div class="slide">
                            <img src="images/wind-blade-2.jpg" alt="Wind Blade Technology 2">
                        </div>
                        <div class="slide">
                            <img src="images/wind-blade-3.jpg" alt="Wind Blade Technology 3">
                        </div>
                        <div class="slide">
                            <img src="images/wind-blade-4.jpg" alt="Wind Blade Technology 4">
                        </div>
                    </div>
                    
                    <div class="slider-navigation">
                        <button class="nav-btn prev-btn"><i class="fa-solid fa-arrow-left"></i></button>
                        <div class="slider-indicators">
                            <span class="indicator active"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                        </div>
                        <button class="nav-btn next-btn"><i class="fa-solid fa-arrow-right"></i></button>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <section class="wind-blade-section" id="wind-blade">
        <div class="container">
            <div class="section-title">
                <h2>About Wind Blade</h2>
                <p>Discover the power of renewable energy with our innovative wind blade technology</p>
            </div>
            
            <div class="wind-blade-content">
                <div class="wind-blade-info">
                    <div class="info-card">
                        <div class="info-icon">
                            <i class="fa-solid fa-wind"></i>
                        </div>
                        <h3>Innovative Design</h3>
                        <p>Our wind blades are engineered with cutting-edge technology to maximize energy efficiency and durability.</p>
                    </div>
                    <div class="info-card">
                        <div class="info-icon">
                            <i class="fa-solid fa-leaf"></i>
                        </div>
                        <h3>Sustainable Energy</h3>
                        <p>Harness the power of wind to generate clean, renewable energy for a sustainable future.</p>
                    </div>
                    <div class="info-card">
                        <div class="info-icon">
                            <i class="fa-solid fa-gear"></i>
                        </div>
                        <h3>Advanced Technology</h3>
                        <p>State-of-the-art materials and manufacturing processes ensure optimal performance.</p>
                    </div>
                </div>
    </section> 
   
    
    <section class="hero">
        <div class="container">
            <div class="hero-content">
                <h1>Find Your <span>Dream Job</span> With Our App</h1>
                <p class="hero-text">Download the Job Portal app and access thousands of job opportunities from your phone. Apply, track applications, and get hired - all in one place.</p>
                <a href="jobportal.apk" class="download-btn pulse">
                    <i class="fa-solid fa-download icon"></i>Download for Android
                </a>
            </div>
            <div class="hero-image">
                <div class="simple-slider">
                    <div class="slider-container">
                        <div class="slide active">
                            <img src="images/app-screenshot-1.png" alt="Job Portal App Screenshot 1">
                        </div>
                        <div class="slide">
                            <img src="images/app-screenshot-2.png" alt="Job Portal App Screenshot 2">
                        </div>
                        <div class="slide">
                            <img src="images/app-screenshot-3.png" alt="Job Portal App Screenshot 3">
                        </div>
                        <div class="slide">
                            <img src="images/app-screenshot-4.png" alt="Job Portal App Screenshot 4">
                        </div>
                        <div class="slide">
                            <img src="images/app-screenshot-5.png" alt="Job Portal App Screenshot 5">
                        </div>
                        <div class="slide">
                            <img src="images/app-screenshot-6.png" alt="Job Portal App Screenshot 6">
                        </div>
                        <div class="slide">
                            <img src="images/app-screenshot-7.png" alt="Job Portal App Screenshot 7">
                        </div>
                    </div>
                    
                    <div class="slider-navigation">
                        <button class="nav-btn prev-btn"><i class="fa-solid fa-arrow-left"></i></button>
                        <div class="slider-indicators">
                            <span class="indicator active"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                            <span class="indicator"></span>
                        </div>
                        <button class="nav-btn next-btn"><i class="fa-solid fa-arrow-right"></i></button>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <section class="features" id="features">
        <div class="container">
            <div class="section-title">
                <h2>App Features</h2>
                <p>Discover what makes our job portal app the best choice for job seekers</p>
            </div>
            <div class="feature-grid">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </div>
                    <h3 class="feature-title">Smart Job Search</h3>
                    <p class="feature-desc">Find relevant jobs with our intelligent search algorithm that matches your skills and experience.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fa-solid fa-paper-plane"></i>
                    </div>
                    <h3 class="feature-title">One-Click Apply</h3>
                    <p class="feature-desc">Apply to jobs with just one click using your saved profile and resume.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fa-solid fa-bell"></i>
                    </div>
                    <h3 class="feature-title">Job Alerts</h3>
                    <p class="feature-desc">Get notified instantly when new jobs matching your preferences are posted.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fa-solid fa-chart-line"></i>
                    </div>
                    <h3 class="feature-title">Application Tracking</h3>
                    <p class="feature-desc">Track the status of your job applications in real-time.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fa-solid fa-lock"></i>
                    </div>
                    <h3 class="feature-title">Secure Profile</h3>
                    <p class="feature-desc">Keep your personal information and documents secure with our encrypted platform.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fa-solid fa-mobile-screen"></i>
                    </div>
                    <h3 class="feature-title">Offline Mode</h3>
                    <p class="feature-desc">View saved jobs and applications even when you're offline.</p>
                </div>
            </div>
        </div>
    </section>
    
    <section class="how-to-download" id="how-to">
        <div class="container">
            <div class="section-title">
                <h2>How to Download</h2>
                <p>Follow these simple steps to get started with our app</p>
            </div>
            <div class="steps">
                <div class="step">
                    <div class="step-number">1</div>
                    <h3 class="step-title">Click Download</h3>
                    <p class="step-desc">Click the download button to start downloading the APK file.</p>
                    <div class="arrow"><i class="fa-solid fa-arrow-right"></i></div>
                </div>
                <div class="step">
                    <div class="step-number">2</div>
                    <h3 class="step-title">Install App</h3>
                    <p class="step-desc">Open the APK file and follow the installation instructions.</p>
                    <div class="arrow"><i class="fa-solid fa-arrow-right"></i></div>
                </div>
                <div class="step">
                    <div class="step-number">3</div>
                    <h3 class="step-title">Create Account</h3>
                    <p class="step-desc">Sign up with your email or mobile number to get started.</p>
                </div>
            </div>
        </div>
    </section>
    
    <section class="cta">
        <div class="container">
            <h2>Ready to Find Your Next Job?</h2>
            <p>Download our app now and start applying to thousands of jobs. Your dream career is just a few taps away.</p>
            <a href="jobportal.apk" class="cta-btn">Download Now (15MB)</a>
        </div>
    </section>
    
    <section class="testimonials" id="testimonials">
        <div class="container">
            <div class="section-title">
                <h2>What Our Users Say</h2>
                <p>Thousands of job seekers have found success with our app</p>
            </div>
            <div class="testimonial-grid">
                <div class="testimonial-card">
                    <p class="testimonial-text">"I found my dream job as a software developer within just 2 weeks of using this app. The interface is incredibly user-friendly and the job matching is spot on!"</p>
                    <div class="testimonial-author">
                        <div class="author-avatar">
                            <img src="https://randomuser.me/api/portraits/men/32.jpg" alt="John Doe">
                        </div>
                        <div>
                            <div class="author-name">John Doe</div>
                            <div class="author-title">Software Developer</div>
                        </div>
                    </div>
                </div>
                <div class="testimonial-card">
                    <p class="testimonial-text">"The job alerts feature saved me so much time. Instead of searching every day, I received notifications for relevant positions and landed a great marketing role."</p>
                    <div class="testimonial-author">
                        <div class="author-avatar">
                            <img src="https://randomuser.me/api/portraits/women/44.jpg" alt="Sarah Johnson">
                        </div>
                        <div>
                            <div class="author-name">Sarah Johnson</div>
                            <div class="author-title">Marketing Specialist</div>
                        </div>
                    </div>
                </div>
                <div class="testimonial-card">
                    <p class="testimonial-text">"As someone who was looking to switch careers, this app was a lifesaver. The variety of jobs and the ease of applying helped me transition to a new industry."</p>
                    <div class="testimonial-author">
                        <div class="author-avatar">
                            <img src="https://randomuser.me/api/portraits/men/67.jpg" alt="Michael Chen">
                        </div>
                        <div>
                            <div class="author-name">Michael Chen</div>
                            <div class="author-title">Financial Analyst</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    
    <footer>
        <div class="container">
            <div class="footer-content">
                <div class="footer-logo">
                    <a href="#">Job Portal</a>
                    <p>Your trusted partner in career advancement. Find jobs, build your career, and connect with employers worldwide.</p>
                </div>
                <div class="footer-links">
                    <h3>Quick Links</h3>
                    <ul>
                        <li><a href="#features">Features</a></li>
                        <li><a href="#how-to">How to Download</a></li>
                        <li><a href="#testimonials">Testimonials</a></li>
                        <li><a href="privacy-policy.html">Privacy Policy</a></li>
                    </ul>
                </div>
                <div class="footer-links">
                    <h3>Contact Us</h3>
                    <ul>
                        <li><a href="mailto:support@jobportal.com">support@jobportal.com</a></li>
                        <li><a href="tel:+123456789">+1 (234) 567-89</a></li>
                        <li><a href="#">FAQ</a></li>
                        <li><a href="#">Help Center</a></li>
                    </ul>
                </div>
            </div>
            <div class="copyright">
                <p>&copy; 2025 Job Portal. All rights reserved.</p>
            </div>
        </div>
    </footer>
    
    <script>
        // Simple script to handle download tracking and carousel functionality
        document.addEventListener('DOMContentLoaded', function() {
            // Download tracking
            const downloadButtons = document.querySelectorAll('a[href="jobportal.apk"]');
            
            downloadButtons.forEach(button => {
                button.addEventListener('click', function(e) {
                    // You can add analytics tracking here
                    console.log('App download initiated');
                    
                    // You can also show a thank you message
                    setTimeout(() => {
                        alert('Thank you for downloading the Job Portal app! The download should start automatically.');
                    }, 1000);
                });
            });
            
            // Carousel functionality
            const carouselItems = document.querySelectorAll('.carousel-item');
            const prevButton = document.querySelector('.carousel-control.prev');
            const nextButton = document.querySelector('.carousel-control.next');
            let currentIndex = 0;
            
            // Auto-rotate the carousel
            let carouselInterval = setInterval(nextSlide, 3000);
            
            function showSlide(index) {
                // Remove active class from all slides
                carouselItems.forEach(item => item.classList.remove('active'));
                
                // Add active class to current slide
                carouselItems[index].classList.add('active');
                
                // Reset the interval timer to prevent quick transitions
                clearInterval(carouselInterval);
                carouselInterval = setInterval(nextSlide, 3000);
            }
            
            function nextSlide() {
                currentIndex = (currentIndex + 1) % carouselItems.length;
                showSlide(currentIndex);
            }
            
            function prevSlide() {
                currentIndex = (currentIndex - 1 + carouselItems.length) % carouselItems.length;
                showSlide(currentIndex);
            }
            
            // Add event listeners to controls
            if (prevButton) prevButton.addEventListener('click', prevSlide);
            if (nextButton) nextButton.addEventListener('click', nextSlide);
            
            // Pause rotation on hover
            const carousel = document.querySelector('.app-carousel');
            if (carousel) {
                carousel.addEventListener('mouseenter', () => {
                    clearInterval(carouselInterval);
                });
                
                carousel.addEventListener('mouseleave', () => {
                    carouselInterval = setInterval(nextSlide, 3000);
                });
            }
        });

        // Simple slider functionality
        document.addEventListener('DOMContentLoaded', function() {
            // Simple script to handle download tracking
            const downloadButtons = document.querySelectorAll('a[href="jobportal.apk"]');
            
            downloadButtons.forEach(button => {
                button.addEventListener('click', function(e) {
                    console.log('App download initiated');
                    setTimeout(() => {
                        alert('Thank you for downloading the Job Portal app! The download should start automatically.');
                    }, 1000);
                });
            });
            
            // Debug: Check if images are loading
            const slideImages = document.querySelectorAll('.slide img');
            console.log(`Found ${slideImages.length} slide images`);
            
            slideImages.forEach((img, index) => {
                console.log(`Image ${index+1} src: ${img.src}`);
                img.onerror = function() {
                    console.error(`Failed to load image: ${img.src}`);
                };
                img.onload = function() {
                    console.log(`Successfully loaded image: ${img.src}`);
                };
            });
            
            // Simple slider functionality
            const slides = document.querySelectorAll('.slide');
            const indicators = document.querySelectorAll('.indicator');
            const prevBtn = document.querySelector('.prev-btn');
            const nextBtn = document.querySelector('.next-btn');
            let currentIndex = 0;
            
            console.log(`Found ${slides.length} slides and ${indicators.length} indicators`);
            
            // Auto-rotate the slider
            let sliderInterval = setInterval(showNextSlide, 4000);
            
            function showSlide(index) {
                // Remove active class from all slides and indicators
                slides.forEach(slide => slide.classList.remove('active'));
                indicators.forEach(indicator => indicator.classList.remove('active'));
                
                // Add active class to current slide and indicator
                slides[index].classList.add('active');
                indicators[index].classList.add('active');
                
                console.log(`Showing slide ${index+1}`);
                
                // Reset the interval timer
                clearInterval(sliderInterval);
                sliderInterval = setInterval(showNextSlide, 4000);
            }
            
            function showNextSlide() {
                currentIndex = (currentIndex + 1) % slides.length;
                showSlide(currentIndex);
            }
            
            function showPrevSlide() {
                currentIndex = (currentIndex - 1 + slides.length) % slides.length;
                showSlide(currentIndex);
            }
            
            // Add event listeners
            if (prevBtn) {
                prevBtn.addEventListener('click', function() {
                    console.log('Previous button clicked');
                    showPrevSlide();
                });
            }
            
            if (nextBtn) {
                nextBtn.addEventListener('click', function() {
                    console.log('Next button clicked');
                    showNextSlide();
                });
            }
            
            // Add click events to indicators
            indicators.forEach((indicator, index) => {
                indicator.addEventListener('click', () => {
                    console.log(`Indicator ${index+1} clicked`);
                    currentIndex = index;
                    showSlide(index);
                });
            });
            
            // Pause rotation on hover
            const sliderContainer = document.querySelector('.slider-container');
            if (sliderContainer) {
                sliderContainer.addEventListener('mouseenter', () => {
                    clearInterval(sliderInterval);
                });
                
                sliderContainer.addEventListener('mouseleave', () => {
                    sliderInterval = setInterval(showNextSlide, 4000);
                });
            }
        });

        // Add Wind Blade Slider functionality
        document.addEventListener('DOMContentLoaded', function() {
            const windBladeSlides = document.querySelectorAll('.wind-blade-slider .slide');
            const windBladeIndicators = document.querySelectorAll('.wind-blade-slider .indicator');
            const windBladePrevBtn = document.querySelector('.wind-blade-slider .prev-btn');
            const windBladeNextBtn = document.querySelector('.wind-blade-slider .next-btn');
            let windBladeCurrentIndex = 0;
            
            let windBladeInterval = setInterval(showNextWindBladeSlide, 4000);
            
            function showWindBladeSlide(index) {
                windBladeSlides.forEach(slide => slide.classList.remove('active'));
                windBladeIndicators.forEach(indicator => indicator.classList.remove('active'));
                
                windBladeSlides[index].classList.add('active');
                windBladeIndicators[index].classList.add('active');
                
                clearInterval(windBladeInterval);
                windBladeInterval = setInterval(showNextWindBladeSlide, 4000);
            }
            
            function showNextWindBladeSlide() {
                windBladeCurrentIndex = (windBladeCurrentIndex + 1) % windBladeSlides.length;
                showWindBladeSlide(windBladeCurrentIndex);
            }
            
            function showPrevWindBladeSlide() {
                windBladeCurrentIndex = (windBladeCurrentIndex - 1 + windBladeSlides.length) % windBladeSlides.length;
                showWindBladeSlide(windBladeCurrentIndex);
            }
            
            if (windBladePrevBtn) {
                windBladePrevBtn.addEventListener('click', showPrevWindBladeSlide);
            }
            
            if (windBladeNextBtn) {
                windBladeNextBtn.addEventListener('click', showNextWindBladeSlide);
            }
            
            windBladeIndicators.forEach((indicator, index) => {
                indicator.addEventListener('click', () => {
                    windBladeCurrentIndex = index;
                    showWindBladeSlide(index);
                });
            });
            
            const windBladeContainer = document.querySelector('.wind-blade-slider .slider-container');
            if (windBladeContainer) {
                windBladeContainer.addEventListener('mouseenter', () => {
                    clearInterval(windBladeInterval);
                });
                
                windBladeContainer.addEventListener('mouseleave', () => {
                    windBladeInterval = setInterval(showNextWindBladeSlide, 4000);
                });
            }
        });
    </script>
</body>
</html> 