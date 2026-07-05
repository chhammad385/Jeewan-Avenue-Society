/**
 * Financial Security Script - Version 3.0
 * Aggressively disables financial buttons for unauthorized users
 * Multiple layers of protection with immediate enforcement
 */

(function() {
    'use strict';
    
    console.log('Financial Security v3.0: Loading...');
    window.financialSecurityInitialized = true;
    
    let currentUserRole = null;
    let securityCheckCount = 0;
    let maxSecurityChecks = 20;
    
    // Get user role with retry mechanism
    async function getUserRole() {
        if (currentUserRole) return currentUserRole;
        
        try {
            const response = await fetch('/api/auth/current-user', {
                credentials: 'include',
                cache: 'no-cache'
            });
            
            if (response.ok) {
                const user = await response.json();
                currentUserRole = user.role;
                console.log('Financial Security: User role confirmed as:', currentUserRole);
                return currentUserRole;
            }
        } catch (error) {
            console.error('Financial Security: Error fetching user role:', error);
        }
        return null;
    }
    
    // Check if user should be restricted
    function isRestrictedUser(role) {
        if (!role) return true;
        
        const normalizedRole = role.toLowerCase().replace(/[_\s-]/g, '');
        const allowedRoles = ['financesecretary', 'finance_secretary', 'finance-secretary', 'president'];
        
        return !allowedRoles.includes(normalizedRole);
    }
    
    // Apply comprehensive security restrictions
    function applyFinancialSecurity() {
        if (!currentUserRole) {
            console.log('Financial Security: No user role yet, skipping...');
            return;
        }
        
        securityCheckCount++;
        console.log(`Financial Security: Applying restrictions (check #${securityCheckCount})`);
        
        if (isRestrictedUser(currentUserRole)) {
            console.log('Financial Security: User restricted - applying all protections');
            
            let buttonsProcessed = 0;
            
            // Find ALL buttons and interactive elements
            const allButtons = [
                ...document.querySelectorAll('button'),
                ...document.querySelectorAll('input[type="submit"]'),
                ...document.querySelectorAll('input[type="button"]'),
                ...document.querySelectorAll('a[onclick]'),
                ...document.querySelectorAll('[onclick]')
            ];
            
            allButtons.forEach(element => {
                const text = element.textContent?.toLowerCase() || '';
                const onclick = element.getAttribute('onclick') || '';
                const id = element.id?.toLowerCase() || '';
                const className = element.className?.toLowerCase() || '';
                
                // Check if this is a financial operation button
                const isFinancialOperation = (
                    text.includes('add') || text.includes('new') || text.includes('create') ||
                    text.includes('edit') || text.includes('update') || text.includes('modify') ||
                    text.includes('delete') || text.includes('remove') || text.includes('save') ||
                    text.includes('submit') || onclick.includes('Record') || 
                    onclick.includes('showModal') || onclick.includes('edit') || onclick.includes('delete') ||
                    id.includes('add') || id.includes('edit') || id.includes('delete') ||
                    className.includes('btn') || window.location.pathname.includes('Financial')
                );
                
                if (isFinancialOperation) {
                    // Maximum button disabling
                    element.disabled = true;
                    element.style.cssText = `
                        opacity: 0.4 !important;
                        cursor: not-allowed !important;
                        pointer-events: none !important;
                        background-color: #cccccc !important;
                        border-color: #999999 !important;
                        color: #666666 !important;
                    `;
                    element.title = 'Access Denied: Finance Secretary role required';
                    
                    // Override all event handlers
                    element.onclick = function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        console.log('Financial Security: Blocked button click');
                        return false;
                    };
                    
                    // Add multiple event listeners to block everything
                    ['click', 'mousedown', 'mouseup', 'touchstart', 'touchend'].forEach(eventType => {
                        element.addEventListener(eventType, function(e) {
                            e.preventDefault();
                            e.stopPropagation();
                            e.stopImmediatePropagation();
                            return false;
                        }, true);
                    });
                    
                    buttonsProcessed++;
                    console.log(`Financial Security: Disabled ${element.tagName} - "${text.trim()}"`);
                }
            });
            
            // Block form submissions
            document.querySelectorAll('form').forEach(form => {
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log('Financial Security: Form submission blocked');
                    return false;
                }, true);
                
                form.onsubmit = function(e) {
                    e.preventDefault();
                    return false;
                };
            });
            
            // Override dangerous window functions
            const dangerousFunctions = ['showModal', 'editRecord', 'deleteRecord', 'handleFormSubmit', 'addRecord', 'newRecord'];
            dangerousFunctions.forEach(funcName => {
                if (window[funcName]) {
                    const originalFunc = window[funcName];
                    window[funcName] = function(...args) {
                        console.log(`Financial Security: Blocked ${funcName} call`);
                        alert('Access Denied: Only Finance Secretary can perform financial operations.');
                        return false;
                    };
                    window[funcName]._originalFunction = originalFunc;
                    window[funcName]._securityBlocked = true;
                }
            });
            
            console.log(`Financial Security: Processed ${buttonsProcessed} elements, blocked ${dangerousFunctions.length} functions`);
            
        } else {
            console.log('Financial Security: User has financial access - no restrictions needed');
        }
    }
    
    // Start aggressive enforcement
    async function startEnforcement() {
        // Get user role first
        await getUserRole();
        
        // Apply security immediately
        applyFinancialSecurity();
        
        // Set up mutation observer for dynamic content
        if (typeof MutationObserver !== 'undefined') {
            const observer = new MutationObserver(function(mutations) {
                let shouldCheck = false;
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                        shouldCheck = true;
                    }
                });
                if (shouldCheck && securityCheckCount < maxSecurityChecks) {
                    setTimeout(applyFinancialSecurity, 50);
                }
            });
            
            observer.observe(document.body, {
                childList: true,
                subtree: true,
                attributes: true,
                attributeFilter: ['onclick', 'disabled', 'style']
            });
        }
        
        // Apply security repeatedly for the first 30 seconds
        let intervalCount = 0;
        const securityInterval = setInterval(function() {
            intervalCount++;
            if (intervalCount < 15 && securityCheckCount < maxSecurityChecks) { // Run 15 times (30 seconds)
                applyFinancialSecurity();
            } else {
                clearInterval(securityInterval);
                console.log('Financial Security: Stopped repeated checks');
            }
        }, 2000);
        
        // One final check after page load
        window.addEventListener('load', function() {
            setTimeout(applyFinancialSecurity, 1000);
        });
    }
    
    // Initialize immediately
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', startEnforcement);
    } else {
        startEnforcement();
    }
    
    console.log('Financial Security: Initialization complete');
    
})();
