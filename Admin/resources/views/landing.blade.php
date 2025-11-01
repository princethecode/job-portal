<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Find Your Dream Job Anywhere in the World - International Job Portal</title>
    <meta name="description" content="Discover thousands of international job opportunities across 50+ countries. Connect with top employers worldwide and start your global career today.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4F46E5;
            --primary-dark: #3730A3;
            --primary-light: #6366F1;
            --secondary-color: #F59E0B;
            --accent-color: #10B981;
            --text-dark: #111827;
            --text-medium: #4B5563;
            --text-light: #6B7280;
            --bg-light: #F9FAFB;
            --bg-white: #FFFFFF;
            --bg-gray: #F3F4F6;
            --shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --shadow-lg: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            --border-radius: 16px;
            --gradient-primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            --gradient-hero: linear-gradient(135deg, #4F46E5 0%, #7C3AED 50%, #EC4899 100%);
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Inter', sans-serif;
            line-height: 1.6;
            color: var(--text-dark);
            background-color: var(--bg-white);
            overflow-x: hidden;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        header {
            background-color: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 1000;
            transition: all 0.3s ease;
        }
        
        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px 0;
        }
        
        .logo {
            font-size: 28px;
            font-weight: 800;
            color: var(--primary-color);
            text-decoration: none;
            letter-spacing: -0.5px;
        }
        
        .nav-links {
            display: flex;
            gap: 32px;
            align-items: center;
        }
        
        .nav-links a {
            text-decoration: none;
            color: var(--text-medium);
            font-weight: 500;
            font-size: 15px;
            transition: all 0.3s ease;
            position: relative;
        }
        
        .nav-links a:hover {
            color: var(--primary-color);
        }
        
        .nav-links a:not(.btn):hover::after {
            content: '';
            position: absolute;
            bottom: -4px;
            left: 0;
            right: 0;
            height: 2px;
            background: var(--primary-color);
            border-radius: 1px;
        }
        
        .hero {
            background: var(--gradient-hero);
            background-image: url('images/hero-bg.jpg');
            background-size: cover;
            background-position: center;
            background-blend-mode: overlay;
            padding: 120px 0 100px;
            color: white;
            position: relative;
            overflow: hidden;
        }
        
        .hero::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(79, 70, 229, 0.8);
            z-index: 1;
        }
        
        .hero-content {
            position: relative;
            z-index: 2;
            text-align: center;
            max-width: 900px;
            margin: 0 auto;
        }
        
        .hero h1 {
            font-size: 3.5rem;
            font-weight: 800;
            margin-bottom: 24px;
            line-height: 1.1;
            letter-spacing: -1px;
        }
        
        .hero h1 .highlight {
            color: #FDE047;
            position: relative;
        }
        
        .hero-subtitle {
            font-size: 1.25rem;
            margin-bottom: 40px;
            opacity: 0.9;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
            font-weight: 400;
        }
        
        .hero-stats {
            display: flex;
            justify-content: center;
            gap: 60px;
            margin: 60px 0 40px;
            flex-wrap: wrap;
        }
        
        .stat-item {
            text-align: center;
        }
        
        .stat-number {
            font-size: 2.5rem;
            font-weight: 800;
            color: #FDE047;
            display: block;
            line-height: 1;
        }
        
        .stat-label {
            font-size: 0.9rem;
            opacity: 0.8;
            margin-top: 8px;
            font-weight: 500;
        }
        
        .btn-primary {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            background: white;
            color: var(--primary-color);
            padding: 16px 32px;
            font-size: 1.1rem;
            font-weight: 600;
            border-radius: 50px;
            text-decoration: none;
            transition: all 0.3s ease;
            cursor: pointer;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
            border: none;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 12px 35px rgba(0, 0, 0, 0.2);
            color: var(--primary-dark);
        }
        
        .btn-secondary {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            background: transparent;
            color: white;
            padding: 16px 32px;
            font-size: 1.1rem;
            font-weight: 600;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-radius: 50px;
            text-decoration: none;
            transition: all 0.3s ease;
            cursor: pointer;
            margin-left: 16px;
        }
        
        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.1);
            border-color: rgba(255, 255, 255, 0.5);
            transform: translateY(-2px);
        }
        
        .brands-section {
            padding: 80px 0;
            background-color: var(--bg-light);
        }
        
        .brands-grid {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 40px;
            margin-top: 60px;
            max-width: 1000px;
            margin-left: auto;
            margin-right: auto;
        }
        
        .brand-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 20px;
            text-align: center;
            transition: all 0.3s ease;
        }
        
        .brand-item:hover {
            transform: translateY(-5px);
        }
        
        .brand-item i {
            font-size: 3rem;
            color: var(--primary-color);
            margin-bottom: 12px;
        }
        
        .brand-item span {
            font-weight: 500;
            color: var(--text-medium);
            font-size: 0.85rem;
        }
        
        .categories-section {
            padding: 80px 0;
            background-color: var(--bg-white);
        }
        
        .categories-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-top: 60px;
            max-width: 1000px;
            margin-left: auto;
            margin-right: auto;
        }
        
        .category-card {
            background: white;
            padding: 24px;
            border-radius: 12px;
            border: 1px solid #E5E7EB;
            transition: all 0.3s ease;
            cursor: pointer;
            display: flex;
            align-items: flex-start;
            gap: 16px;
        }
        
        .category-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            border-color: var(--primary-color);
        }
        
        .category-icon {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }
        
        .category-icon.healthcare {
            background-color: #FEF2F2;
            color: #EF4444;
        }
        
        .category-icon.technology {
            background-color: #EFF6FF;
            color: #3B82F6;
        }
        
        .category-icon.hospitality {
            background-color: #FEF3C7;
            color: #F59E0B;
        }
        
        .category-icon.logistics {
            background-color: #ECFDF5;
            color: #10B981;
        }
        
        .category-icon.education {
            background-color: #F3E8FF;
            color: #8B5CF6;
        }
        
        .category-icon.construction {
            background-color: #FEF3C7;
            color: #D97706;
        }
        
        .category-icon.engineering {
            background-color: #EFF6FF;
            color: #6366F1;
        }
        
        .category-icon.business {
            background-color: #FCE7F3;
            color: #EC4899;
        }
        
        .category-icon i {
            font-size: 20px;
        }
        
        .category-content {
            flex: 1;
        }
        
        .category-title {
            font-size: 1rem;
            font-weight: 600;
            margin-bottom: 4px;
            color: var(--text-dark);
        }
        
        .category-count {
            color: var(--text-light);
            font-size: 0.875rem;
        }
        
        .destinations-section {
            padding: 80px 0;
            background-color: var(--bg-light);
        }
        
        .destinations-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 30px;
            margin-top: 60px;
            max-width: 1000px;
            margin-left: auto;
            margin-right: auto;
        }
        
        .destination-card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            border: 1px solid #E5E7EB;
            transition: all 0.3s ease;
            cursor: pointer;
        }
        
        .destination-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            border-color: var(--primary-color);
        }
        
        .destination-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 12px;
        }
        
        .destination-flag {
            width: 60px;
            height: 36px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.8rem;
            border: 0px solid #E5E7EB;
        }
        
        .destination-name {
            font-size: 1.1rem;
            font-weight: 600;
            color: var(--text-dark);
        }
        
        .destination-description {
            color: var(--text-light);
            font-size: 0.875rem;
            margin-bottom: 16px;
            line-height: 1.4;
        }
        
        .destination-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .destination-jobs {
            color: var(--primary-color);
            font-weight: 600;
            font-size: 0.875rem;
        }
        
        .destination-link {
            color: var(--text-medium);
            text-decoration: none;
            font-weight: 500;
            font-size: 0.875rem;
            display: flex;
            align-items: center;
            gap: 4px;
        }
        
        .destination-link:hover {
            color: var(--primary-color);
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
        
        .cta-section {
            padding: 100px 0;
            background: var(--gradient-hero);
            color: white;
            text-align: center;
            position: relative;
            overflow: hidden;
        }
        
        .cta-section::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(79, 70, 229, 0.1);
            z-index: 1;
        }
        
        .cta-content {
            position: relative;
            z-index: 2;
            max-width: 800px;
            margin: 0 auto;
        }
        
        .cta-section h2 {
            font-size: 3rem;
            font-weight: 800;
            margin-bottom: 24px;
            line-height: 1.2;
        }
        
        .cta-section p {
            font-size: 1.25rem;
            margin-bottom: 40px;
            opacity: 0.9;
            line-height: 1.6;
        }
        
        .cta-buttons {
            display: flex;
            gap: 20px;
            justify-content: center;
            flex-wrap: wrap;
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
                padding: 100px 0 80px;
            }
            
            .hero h1 {
                font-size: 2.5rem;
            }
            
            .hero-stats {
                gap: 40px;
                margin: 40px 0 30px;
            }
            
            .stat-number {
                font-size: 2rem;
            }
            
            .nav-links {
                display: none;
            }
            
            .section-title h2 {
                font-size: 2rem;
            }
            
            .cta-section h2 {
                font-size: 2rem;
            }
            
            .cta-buttons {
                flex-direction: column;
                align-items: center;
            }
            
            .btn-primary, .btn-secondary {
                width: 100%;
                max-width: 300px;
                justify-content: center;
                margin: 0;
            }
            
            .btn-secondary {
                margin-top: 16px;
            }
            
            .brands-grid {
                grid-template-columns: repeat(3, 1fr);
                gap: 20px;
            }
            
            .categories-grid {
                grid-template-columns: repeat(2, 1fr);
                gap: 16px;
                max-width: none;
            }
            
            .destinations-grid {
                grid-template-columns: 1fr;
                gap: 20px;
            }
        }
        
        /* Animations */
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .fade-in-up {
            animation: fadeInUp 0.6s ease-out;
        }
        
        @keyframes float {
            0%, 100% {
                transform: translateY(0px);
            }
            50% {
                transform: translateY(-10px);
            }
        }
        
        .floating {
            animation: float 3s ease-in-out infinite;
        }
    </style>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <header>
        <div class="container">
            <nav class="navbar">
                <a href="#" class="logo">Abroad Jobs</a>
                <div class="nav-links">
                    <a href="#brands">Companies</a>
                    <a href="#categories">Job Categories</a>
                    <a href="#destinations">Destinations</a>
                    <a href="#testimonials">Reviews</a>
                    <a href="jobportal.apk" class="btn-primary">
                        <i class="fa-solid fa-download"></i>Download App
                    </a>
                </div>
            </nav>
        </div>
    </header>
    <section class="hero">
        <div class="container">
            <div class="hero-content fade-in-up">
                <h1>Find Your Dream Job <span class="highlight">Anywhere in the World</span></h1>
                <p class="hero-subtitle">Discover thousands of international job opportunities across 50+ countries. Connect with top employers worldwide and start your global career today.</p>
                
                <div class="hero-stats">
                    <div class="stat-item">
                        <span class="stat-number">10,000+</span>
                        <span class="stat-label">Active Jobs</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">50+</span>
                        <span class="stat-label">Countries</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">500+</span>
                        <span class="stat-label">Companies</span>
                    </div>
                </div>
                
                <div class="cta-buttons">
                    <a href="jobportal.apk" class="btn-primary">
                        <i class="fa-solid fa-download"></i>Download App
                    </a>
                    <a href="#categories" class="btn-secondary">
                        <i class="fa-solid fa-search"></i>Browse Jobs
                    </a>
                </div>
            </div>
        </div>
    </section>
    <section class="brands-section" id="brands">
        <div class="container">
            <div class="section-title">
                <h2><span style="color: var(--primary-color);">Brands</span> That Hire Through Abroad Jobs</h2>
                <p>Jobs posted by the best direct companies worldwide. Check jobs now!</p>
            </div>
            <div class="brands-grid">
                <div class="brand-item">
                    <i class="fa-solid fa-building"></i>
                    <span>Global Tech Solutions</span>
                </div>
                <div class="brand-item">
                    <i class="fa-solid fa-briefcase-medical"></i>
                    <span>International Healthcare</span>
                </div>
                <div class="brand-item">
                    <i class="fa-solid fa-globe"></i>
                    <span>WorldWide Industries</span>
                </div>
                <div class="brand-item">
                    <i class="fa-solid fa-building"></i>
                    <span>Elite Construction</span>
                </div>
                <div class="brand-item">
                    <i class="fa-solid fa-briefcase-medical"></i>
                    <span>Premium Hospitality</span>
                </div>
                <div class="brand-item">
                    <i class="fa-solid fa-globe"></i>
                    <span>Tech Innovators</span>
                </div>
            </div>
        </div>
    </section>
    
    <section class="categories-section" id="categories">
        <div class="container">
            <div class="section-title">
                <h2>Popular Job Categories</h2>
                <p>Explore opportunities across diverse industries and find your perfect match</p>
            </div>
            <div class="categories-grid">
                <div class="category-card">
                    <div class="category-icon healthcare">
                        <i class="fa-solid fa-stethoscope"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Healthcare</h3>
                        <p class="category-count">1240 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon technology">
                        <i class="fa-solid fa-code"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">IT & Technology</h3>
                        <p class="category-count">2180 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon hospitality">
                        <i class="fa-solid fa-utensils"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Hospitality</h3>
                        <p class="category-count">890 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon logistics">
                        <i class="fa-solid fa-truck"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Logistics</h3>
                        <p class="category-count">670 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon education">
                        <i class="fa-solid fa-graduation-cap"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Education</h3>
                        <p class="category-count">540 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon construction">
                        <i class="fa-solid fa-hammer"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Construction</h3>
                        <p class="category-count">920 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon engineering">
                        <i class="fa-solid fa-cog"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Engineering</h3>
                        <p class="category-count">1100 jobs available</p>
                    </div>
                </div>
                <div class="category-card">
                    <div class="category-icon business">
                        <i class="fa-solid fa-briefcase"></i>
                    </div>
                    <div class="category-content">
                        <h3 class="category-title">Business</h3>
                        <p class="category-count">1560 jobs available</p>
                    </div>
                </div>
            </div>
        </div>
    </section>
    
    <section class="destinations-section" id="destinations">
        <div class="container">
            <div class="section-title">
                <h2>Top Destinations for International Jobs</h2>
                <p>Discover career opportunities in the world's most sought-after countries</p>
            </div>
            <div class="destinations-grid">
                <div class="destination-card">
                    <div class="destination-header">
                        <div class="destination-flag">🇦🇪</div>
                        <h3 class="destination-name">United Arab Emirates</h3>
                    </div>
                    <p class="destination-description">Tax-free salaries and modern infrastructure</p>
                    <div class="destination-footer">
                        <span class="destination-jobs">3,240 jobs available</span>
                        <a href="#" class="destination-link">Explore <i class="fa-solid fa-arrow-right"></i></a>
                    </div>
                </div>
                <div class="destination-card">
                    <div class="destination-header">
                        <div class="destination-flag">🇬🇧</div>
                        <h3 class="destination-name">United Kingdom</h3>
                    </div>
                    <p class="destination-description">Rich culture and career development</p>
                    <div class="destination-footer">
                        <span class="destination-jobs">2,800 jobs available</span>
                        <a href="#" class="destination-link">Explore <i class="fa-solid fa-arrow-right"></i></a>
                    </div>
                </div>
                <div class="destination-card">
                    <div class="destination-header">
                        <div class="destination-flag">🇨🇦</div>
                        <h3 class="destination-name">Canada</h3>
                    </div>
                    <p class="destination-description">High quality of life and immigration pathways</p>
                    <div class="destination-footer">
                        <span class="destination-jobs">2,150 jobs available</span>
                        <a href="#" class="destination-link">Explore <i class="fa-solid fa-arrow-right"></i></a>
                    </div>
                </div>
                <div class="destination-card">
                    <div class="destination-header">
                        <div class="destination-flag">🇶🇦</div>
                        <h3 class="destination-name">Qatar</h3>
                    </div>
                    <p class="destination-description">Tax-free income and world-class infrastructure</p>
                    <div class="destination-footer">
                        <span class="destination-jobs">1,920 jobs available</span>
                        <a href="#" class="destination-link">Explore <i class="fa-solid fa-arrow-right"></i></a>
                    </div>
                </div>
                <div class="destination-card">
                    <div class="destination-header">
                        <div class="destination-flag">🇸🇬</div>
                        <h3 class="destination-name">Singapore</h3>
                    </div>
                    <p class="destination-description">Global business hub and innovation center</p>
                    <div class="destination-footer">
                        <span class="destination-jobs">1,680 jobs available</span>
                        <a href="#" class="destination-link">Explore <i class="fa-solid fa-arrow-right"></i></a>
                    </div>
                </div>
                <div class="destination-card">
                    <div class="destination-header">
                        <div class="destination-flag">🇴🇲</div>
                        <h3 class="destination-name">Oman</h3>
                    </div>
                    <p class="destination-description">Growing economy and cultural diversity</p>
                    <div class="destination-footer">
                        <span class="destination-jobs">1,540 jobs available</span>
                        <a href="#" class="destination-link">Explore <i class="fa-solid fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
        </div>
    </section>
    
    <section class="cta-section">
        <div class="container">
            <div class="cta-content">
                <h2>Ready to Start Your International Career?</h2>
                <p>Join thousands of professionals who have found their dream jobs abroad. Download our app and take the first step towards your global career today.</p>
                <div class="cta-buttons">
                    <a href="jobportal.apk" class="btn-primary">
                        <i class="fa-solid fa-download"></i>Download App Now
                    </a>
                    <a href="#categories" class="btn-secondary">
                        <i class="fa-solid fa-search"></i>Browse Jobs
                    </a>
                </div>
            </div>
        </div>
    </section>
    
    <section class="testimonials" id="testimonials">
        <div class="container">
            <div class="section-title">
                <h2>What <span style="color: var(--primary-color);">People Say</span> About Us</h2>
                <p>Thousands of job seekers have found success with our platform</p>
            </div>
            <div class="testimonial-grid">
                <div class="testimonial-card">
                    <p class="testimonial-text">"I found my dream job as a software developer in Canada within just 3 weeks of using Abroad Jobs. The platform made the entire process seamless and stress-free."</p>
                    <div class="testimonial-author">
                        <div class="author-avatar">
                            <img src="https://randomuser.me/api/portraits/men/32.jpg" alt="John Smith">
                        </div>
                        <div>
                            <div class="author-name">John Smith</div>
                            <div class="author-title">Software Developer, Canada</div>
                        </div>
                    </div>
                </div>
                <div class="testimonial-card">
                    <p class="testimonial-text">"The job alerts feature saved me so much time. I received notifications for relevant positions in the UK and landed a great marketing role with a top company."</p>
                    <div class="testimonial-author">
                        <div class="author-avatar">
                            <img src="https://randomuser.me/api/portraits/women/44.jpg" alt="Sarah Johnson">
                        </div>
                        <div>
                            <div class="author-name">Sarah Johnson</div>
                            <div class="author-title">Marketing Manager, UK</div>
                        </div>
                    </div>
                </div>
                <div class="testimonial-card">
                    <p class="testimonial-text">"As someone looking to work in Australia, this platform was a lifesaver. The variety of jobs and ease of application helped me secure my dream position."</p>
                    <div class="testimonial-author">
                        <div class="author-avatar">
                            <img src="https://randomuser.me/api/portraits/men/67.jpg" alt="Michael Chen">
                        </div>
                        <div>
                            <div class="author-name">Michael Chen</div>
                            <div class="author-title">Financial Analyst, Australia</div>
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
                    <a href="#">Abroad Jobs</a>
                    <p>Your gateway to international career opportunities. Connect with global employers and build your dream career anywhere in the world.</p>
                </div>
                <div class="footer-links">
                    <h3>Quick Links</h3>
                    <ul>
                        <li><a href="#brands">Companies</a></li>
                        <li><a href="#categories">Job Categories</a></li>
                        <li><a href="#destinations">Destinations</a></li>
                        <li><a href="privacy-policy.html">Privacy Policy</a></li>
                        <li><a href="terms-of-service.html">Terms of Service</a></li>
                    </ul>
                </div>
                <div class="footer-links">
                    <h3>For Job Seekers</h3>
                    <ul>
                        <li><a href="#">Browse Jobs</a></li>
                        <li><a href="#">Career Advice</a></li>
                        <li><a href="#">Resume Builder</a></li>
                        <li><a href="#">Salary Guide</a></li>
                    </ul>
                </div>
                <div class="footer-links">
                    <h3>Contact Us</h3>
                    <ul>
                        <li><a href="mailto:support@abroadjobs.com">support@emps.com</a></li>
                        <li><a href="tel:+123456789">+1 (234) 567-89</a></li>
                        <li><a href="#">FAQ</a></li>
                        <li><a href="#">Help Center</a></li>
                    </ul>
                </div>
            </div>
            <div class="copyright">
                <p>&copy; 2025 Abroad Jobs. All rights reserved.</p>
            </div>
        </div>
    </footer>
    
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Download tracking
            const downloadButtons = document.querySelectorAll('a[href="jobportal.apk"]');
            
            downloadButtons.forEach(button => {
                button.addEventListener('click', function(e) {
                    console.log('App download initiated');
                    setTimeout(() => {
                        alert('Thank you for downloading the Abroad Jobs app! The download should start automatically.');
                    }, 1000);
                });
            });
            
            // Smooth scrolling for navigation links
            const navLinks = document.querySelectorAll('a[href^="#"]');
            navLinks.forEach(link => {
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    const targetId = this.getAttribute('href');
                    const targetSection = document.querySelector(targetId);
                    if (targetSection) {
                        targetSection.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start'
                        });
                    }
                });
            });
            
            // Add scroll effect to header
            const header = document.querySelector('header');
            window.addEventListener('scroll', function() {
                if (window.scrollY > 100) {
                    header.style.background = 'rgba(255, 255, 255, 0.98)';
                    header.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
                } else {
                    header.style.background = 'rgba(255, 255, 255, 0.95)';
                    header.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
                }
            });
            
            // Add animation on scroll
            const observerOptions = {
                threshold: 0.1,
                rootMargin: '0px 0px -50px 0px'
            };
            
            const observer = new IntersectionObserver(function(entries) {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('fade-in-up');
                    }
                });
            }, observerOptions);
            
            // Observe elements for animation
            const animateElements = document.querySelectorAll('.section-title, .category-card, .destination-card, .brand-item');
            animateElements.forEach(el => observer.observe(el));
        });
    </script>
</body>
</html> 