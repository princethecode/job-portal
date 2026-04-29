<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coming Soon</title>

    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #4facfe, #00f2fe);
            color: #fff;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            text-align: center;
        }

        .container {
            max-width: 500px;
        }

        h1 {
            font-size: 48px;
            margin-bottom: 10px;
        }

        p {
            font-size: 18px;
            margin-bottom: 30px;
        }

        #countdown {
            font-size: 24px;
            margin-bottom: 30px;
        }

        input {
            padding: 12px;
            width: 70%;
            border: none;
            border-radius: 5px;
            outline: none;
        }

        button {
            padding: 12px 20px;
            border: none;
            background: #fff;
            color: #333;
            border-radius: 5px;
            cursor: pointer;
            margin-left: 10px;
        }

        button:hover {
            background: #eee;
        }

        footer {
            margin-top: 30px;
            font-size: 14px;
            opacity: 0.8;
        }
    </style>
</head>

<body>
    <div class="container">
        <h1>Coming Soon 🚀</h1>
        <p>We’re working hard to launch something amazing. Stay tuned!</p>

        <div id="countdown"></div>

        <div>
            <input type="email" placeholder="Enter your email">
            <button>Notify Me</button>
        </div>

        <footer>
            © 2026 Your Company
        </footer>
    </div>

    <script>
        // Set launch date
        const launchDate = new Date("May 30, 2026 00:00:00").getTime();

        const countdown = setInterval(() => {
            const now = new Date().getTime();
            const diff = launchDate - now;

            const days = Math.floor(diff / (1000 * 60 * 60 * 24));
            const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((diff % (1000 * 60)) / 1000);

            document.getElementById("countdown").innerHTML =
                `${days}d ${hours}h ${minutes}m ${seconds}s`;

            if (diff < 0) {
                clearInterval(countdown);
                document.getElementById("countdown").innerHTML = "We are live!";
            }
        }, 1000);
    </script>
</body>
</html>