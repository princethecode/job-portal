<?php
/**
 * Quick PHP Upload Limits Checker with Auto-Refresh
 * Visit this file in your browser to see current limits
 * DELETE THIS FILE after checking for security!
 */

header('Content-Type: text/html; charset=utf-8');
?>
<!DOCTYPE html>
<html>
<head>
    <title>PHP Upload Limits Check</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); max-width: 600px; margin: 0 auto; }
        h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }
        .auto-refresh { background: #fff3e0; padding: 15px; border-radius: 4px; margin-bottom: 20px; border-left: 4px solid #ff9800; }
        .auto-refresh button { padding: 8px 16px; margin: 5px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
        .auto-refresh button.active { background: #4CAF50; color: white; }
        .auto-refresh button.inactive { background: #ccc; color: #666; }
        .countdown { font-weight: bold; color: #ff9800; }
        .last-check { color: #666; font-size: 12px; margin-top: 5px; }
        .setting { margin: 15px 0; padding: 15px; background: #f9f9f9; border-left: 4px solid #4CAF50; transition: all 0.3s ease; }
        .setting.warning { border-left-color: #ff9800; background: #fff3e0; }
        .setting.error { border-left-color: #f44336; background: #ffebee; }
        .setting.changed { animation: highlight 1s ease; }
        @keyframes highlight { 0% { background: #ffeb3b; } 100% { background: inherit; } }
        .label { font-weight: bold; color: #555; }
        .value { font-size: 18px; color: #333; margin-top: 5px; }
        .status { display: inline-block; padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: bold; margin-left: 10px; }
        .status.ok { background: #4CAF50; color: white; }
        .status.warning { background: #ff9800; color: white; }
        .status.error { background: #f44336; color: white; }
        .recommendation { margin-top: 30px; padding: 20px; background: #e3f2fd; border-radius: 4px; border-left: 4px solid #2196F3; }
        .delete-warning { margin-top: 30px; padding: 20px; background: #ffebee; border-radius: 4px; border-left: 4px solid #f44336; color: #c62828; font-weight: bold; }
        .loading { display: inline-block; width: 12px; height: 12px; border: 2px solid #f3f3f3; border-top: 2px solid #4CAF50; border-radius: 50%; animation: spin 1s linear infinite; margin-left: 10px; }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
    </style>
</head>
<body>
    <div class="container">
        <h1>📊 PHP Upload Limits Check</h1>
        
        <div class="auto-refresh">
            <strong>🔄 Auto-Refresh:</strong>
            <button id="toggleRefresh" class="active" onclick="toggleAutoRefresh()">ON</button>
            <button onclick="checkNow()">Check Now</button>
            <div class="countdown">
                Next check in: <span id="countdown">30</span> seconds
                <span id="loading" class="loading" style="display: none;"></span>
            </div>
            <div class="last-check">Last checked: <span id="lastCheck">Just now</span></div>
        </div>
        
        <?php
        // Get current settings
        $upload_max = ini_get('upload_max_filesize');
        $post_max = ini_get('post_max_size');
        $memory_limit = ini_get('memory_limit');
        $max_execution = ini_get('max_execution_time');
        $max_input = ini_get('max_input_time');
        
        // Convert to bytes for comparison
        function convertToBytes($value) {
            $value = trim($value);
            $last = strtolower($value[strlen($value)-1]);
            $value = (int)$value;
            switch($last) {
                case 'g': $value *= 1024;
                case 'm': $value *= 1024;
                case 'k': $value *= 1024;
            }
            return $value;
        }
        
        $upload_bytes = convertToBytes($upload_max);
        $post_bytes = convertToBytes($post_max);
        $memory_bytes = convertToBytes($memory_limit);
        $target_bytes = 10 * 1024 * 1024; // 10MB
        
        // Check status
        $upload_ok = $upload_bytes >= $target_bytes;
        $post_ok = $post_bytes >= ($target_bytes + 2*1024*1024); // Should be 12MB
        $memory_ok = $memory_bytes >= 128*1024*1024; // 128MB
        $execution_ok = $max_execution >= 300;
        $input_ok = $max_input >= 300;
        
        $all_ok = $upload_ok && $post_ok && $memory_ok && $execution_ok && $input_ok;
        ?>
        
        <div class="setting <?php echo $upload_ok ? '' : 'error'; ?>">
            <div class="label">upload_max_filesize</div>
            <div class="value">
                <span data-setting="upload_max"><?php echo $upload_max; ?></span>
                <span class="status <?php echo $upload_ok ? 'ok' : 'error'; ?>">
                    <?php echo $upload_ok ? '✓ OK' : '✗ TOO LOW'; ?>
                </span>
            </div>
            <?php if (!$upload_ok): ?>
                <small>Required: 10M or higher</small>
            <?php endif; ?>
        </div>
        
        <div class="setting <?php echo $post_ok ? '' : 'error'; ?>">
            <div class="label">post_max_size</div>
            <div class="value">
                <span data-setting="post_max"><?php echo $post_max; ?></span>
                <span class="status <?php echo $post_ok ? 'ok' : 'error'; ?>">
                    <?php echo $post_ok ? '✓ OK' : '✗ TOO LOW'; ?>
                </span>
            </div>
            <?php if (!$post_ok): ?>
                <small>Required: 12M or higher (must be larger than upload_max_filesize)</small>
            <?php endif; ?>
        </div>
        
        <div class="setting <?php echo $memory_ok ? '' : 'warning'; ?>">
            <div class="label">memory_limit</div>
            <div class="value">
                <span data-setting="memory_limit"><?php echo $memory_limit; ?></span>
                <span class="status <?php echo $memory_ok ? 'ok' : 'warning'; ?>">
                    <?php echo $memory_ok ? '✓ OK' : '⚠ LOW'; ?>
                </span>
            </div>
            <?php if (!$memory_ok): ?>
                <small>Recommended: 128M or higher</small>
            <?php endif; ?>
        </div>
        
        <div class="setting <?php echo $execution_ok ? '' : 'warning'; ?>">
            <div class="label">max_execution_time</div>
            <div class="value">
                <span data-setting="max_execution"><?php echo $max_execution; ?> seconds</span>
                <span class="status <?php echo $execution_ok ? 'ok' : 'warning'; ?>">
                    <?php echo $execution_ok ? '✓ OK' : '⚠ LOW'; ?>
                </span>
            </div>
            <?php if (!$execution_ok): ?>
                <small>Recommended: 300 seconds or higher for large uploads</small>
            <?php endif; ?>
        </div>
        
        <div class="setting <?php echo $input_ok ? '' : 'warning'; ?>">
            <div class="label">max_input_time</div>
            <div class="value">
                <span data-setting="max_input"><?php echo $max_input; ?> seconds</span>
                <span class="status <?php echo $input_ok ? 'ok' : 'warning'; ?>">
                    <?php echo $input_ok ? '✓ OK' : '⚠ LOW'; ?>
                </span>
            </div>
            <?php if (!$input_ok): ?>
                <small>Recommended: 300 seconds or higher for large uploads</small>
            <?php endif; ?>
        </div>
        
        <div class="recommendation">
            <h3>📋 Recommendation:</h3>
            <?php if ($all_ok): ?>
                <p><strong style="color: #4CAF50;">✓ All settings are optimal!</strong></p>
                <p>Your server is configured correctly for 10MB file uploads. You <strong>DO NOT need</strong> the <code>.user.ini</code> file.</p>
                <p><strong>Action:</strong> You can safely delete the <code>.user.ini</code> file from the Admin folder.</p>
            <?php else: ?>
                <p><strong style="color: #f44336;">✗ Some settings need adjustment</strong></p>
                <p>Your server settings are below the recommended values for 10MB uploads.</p>
                <p><strong>Action:</strong> Keep the <code>.user.ini</code> file and wait 5 minutes for it to take effect, then refresh this page.</p>
                <p>If settings don't change after 5 minutes, you may need to:</p>
                <ul>
                    <li>Update PHP settings via cPanel/Plesk</li>
                    <li>Contact your hosting provider</li>
                    <li>Edit the server's php.ini file (if you have access)</li>
                </ul>
            <?php endif; ?>
        </div>
        
        <div class="delete-warning">
            <strong>⚠️ SECURITY WARNING:</strong> Delete this file (check_php_limits.php) after checking your settings!
        </div>
    </div>
    
    <script>
        let autoRefreshEnabled = true;
        let countdownSeconds = 30;
        let countdownInterval;
        let refreshInterval;
        let previousValues = {};
        
        // Store initial values
        document.addEventListener('DOMContentLoaded', function() {
            storeCurrentValues();
            startCountdown();
        });
        
        function storeCurrentValues() {
            previousValues = {
                upload_max: document.querySelector('[data-setting="upload_max"]')?.textContent || '',
                post_max: document.querySelector('[data-setting="post_max"]')?.textContent || '',
                memory_limit: document.querySelector('[data-setting="memory_limit"]')?.textContent || '',
                max_execution: document.querySelector('[data-setting="max_execution"]')?.textContent || '',
                max_input: document.querySelector('[data-setting="max_input"]')?.textContent || ''
            };
        }
        
        function toggleAutoRefresh() {
            autoRefreshEnabled = !autoRefreshEnabled;
            const btn = document.getElementById('toggleRefresh');
            
            if (autoRefreshEnabled) {
                btn.textContent = 'ON';
                btn.className = 'active';
                startCountdown();
            } else {
                btn.textContent = 'OFF';
                btn.className = 'inactive';
                stopCountdown();
            }
        }
        
        function startCountdown() {
            if (!autoRefreshEnabled) return;
            
            countdownSeconds = 30;
            updateCountdownDisplay();
            
            clearInterval(countdownInterval);
            countdownInterval = setInterval(function() {
                countdownSeconds--;
                updateCountdownDisplay();
                
                if (countdownSeconds <= 0) {
                    checkNow();
                }
            }, 1000);
        }
        
        function stopCountdown() {
            clearInterval(countdownInterval);
            document.getElementById('countdown').textContent = '--';
        }
        
        function updateCountdownDisplay() {
            document.getElementById('countdown').textContent = countdownSeconds;
        }
        
        function checkNow() {
            // Show loading indicator
            document.getElementById('loading').style.display = 'inline-block';
            
            // Reload the page
            location.reload();
        }
        
        function updateLastCheckTime() {
            const now = new Date();
            const timeString = now.toLocaleTimeString();
            document.getElementById('lastCheck').textContent = timeString;
        }
        
        // Check if values changed (for highlighting)
        function checkForChanges() {
            const settings = document.querySelectorAll('.setting');
            settings.forEach(setting => {
                const dataAttr = setting.querySelector('[data-setting]');
                if (dataAttr) {
                    const key = dataAttr.getAttribute('data-setting');
                    const currentValue = dataAttr.textContent;
                    
                    if (previousValues[key] && previousValues[key] !== currentValue) {
                        setting.classList.add('changed');
                        setTimeout(() => setting.classList.remove('changed'), 2000);
                    }
                }
            });
        }
        
        // Update last check time on load
        updateLastCheckTime();
        checkForChanges();
    </script>
</body>
</html>
