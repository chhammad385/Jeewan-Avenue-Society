/**
 * Admin Button Restriction System
 * Disables specific action buttons within tabs based on user roles
 * All tabs remain accessible, only buttons are restricted
 */

(function() {
    'use strict';
    
    console.log('Admin Button Restrictions: Loading role-based button restrictions...');
    
    let currentUserRole = null;
    let restrictionsApplied = false;
    
    // Add CSS for disabled buttons
    const style = document.createElement('style');
    style.textContent = `
        .button-restricted {
            opacity: 0.4 !important;
            cursor: not-allowed !important;
            pointer-events: none !important;
            background-color: #e9ecef !important;
            border-color: #dee2e6 !important;
            color: #6c757d !important;
            position: relative !important;
        }
        
        .button-restricted:hover {
            background-color: #e9ecef !important;
            border-color: #dee2e6 !important;
            color: #6c757d !important;
            transform: none !important;
        }
        
        .button-restricted::after {
            content: "🚫" !important;
            position: absolute !important;
            top: 50% !important;
            left: 50% !important;
            transform: translate(-50%, -50%) !important;
            font-size: 12px !important;
            z-index: 1000 !important;
        }
        
        .role-info-badge {
            position: fixed !important;
            top: 20px !important;
            right: 20px !important;
            background: linear-gradient(135deg, #13A89E, #0f8b82) !important;
            color: white !important;
            padding: 10px 15px !important;
            border-radius: 6px !important;
            font-size: 12px !important;
            font-weight: bold !important;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2) !important;
            z-index: 10000 !important;
            animation: fadeIn 0.5s ease-out !important;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(style);
    
    // Role-based button permissions
    const BUTTON_PERMISSIONS = {
        // Financial buttons - Finance Secretary ONLY
        'financial': {
            add: ['Finance-Secretary', 'Finance_Secretary', 'FINANCE_SECRETARY'],
            edit: ['Finance-Secretary', 'Finance_Secretary', 'FINANCE_SECRETARY'],
            delete: ['Finance-Secretary', 'Finance_Secretary', 'FINANCE_SECRETARY']
        },
        
        // Announcement buttons - Information Secretary ONLY  
        'announcement': {
            create: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY'],
            upload: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY'],
            edit: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY'],
            delete: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY']
        },
        
        // Document buttons - Information Secretary ONLY
        'document': {
            upload: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY'],
            delete: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY']
        },
        
        // Renter document buttons - General Secretary ONLY
        'renter': {
            add: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY'],
            edit: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY'],
            delete: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY']
        },
        
        // Member management - General Secretary & President
        'member': {
            add: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            edit: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            delete: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT']
        },
        
        // User management - General Secretary & President
        'user': {
            add: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            edit: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            delete: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT']
        },
        
        // Plot management - General Secretary & President
        'plot': {
            add: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            edit: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            delete: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            save: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT']
        }
    };
    
    // Get user role from API
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
                console.log('Admin Tab Restrictions: User role confirmed as:', currentUserRole);
                return currentUserRole;
            }
        } catch (error) {
            console.error('Admin Tab Restrictions: Error fetching user role:', error);
        }
        return null;
    }
    
    // Normalize role for comparison
    function normalizeRole(role) {
        if (!role) return '';
        return role.toLowerCase().replace(/[_\s-]/g, '');
    }
    
    // Check if user has permission for specific button action
    function hasButtonPermission(buttonType, action, userRole) {
        if (!userRole || !BUTTON_PERMISSIONS[buttonType] || !BUTTON_PERMISSIONS[buttonType][action]) {
            return true; // Default allow if no restrictions defined
        }
        
        const allowedRoles = BUTTON_PERMISSIONS[buttonType][action];
        const normalizedUserRole = normalizeRole(userRole);
        
        return allowedRoles.some(allowedRole => 
            normalizeRole(allowedRole) === normalizedUserRole
        );
    }
    
    // Apply button restrictions based on button text and context
    function applyButtonRestrictions(userRole) {
        if (!userRole) return;
        
        console.log('Admin Button Restrictions: Applying button restrictions for role:', userRole);
        
        // Find all buttons and apply restrictions
        const buttons = document.querySelectorAll('button, input[type="button"], input[type="submit"], .btn, [onclick]');
        let restrictedCount = 0;
        
        buttons.forEach(button => {
            const buttonText = (button.textContent || button.value || '').toLowerCase();
            const buttonId = (button.id || '').toLowerCase();
            const buttonClass = (button.className || '').toLowerCase();
            
            let shouldRestrict = false;
            let buttonType = '';
            let action = '';
            
            // Determine button type and action based on text, ID, or class
            if (buttonText.includes('add') || buttonText.includes('create') || buttonText.includes('new')) {
                action = 'add';
                if (buttonText.includes('financial') || buttonId.includes('financial') || buttonClass.includes('financial')) {
                    buttonType = 'financial';
                } else if (buttonText.includes('member') || buttonId.includes('member') || buttonClass.includes('member')) {
                    buttonType = 'member';
                } else if (buttonText.includes('user') || buttonId.includes('user') || buttonClass.includes('user')) {
                    buttonType = 'user';
                } else if (buttonText.includes('plot') || buttonId.includes('plot') || buttonClass.includes('plot')) {
                    buttonType = 'plot';
                } else if (buttonText.includes('renter') || buttonId.includes('renter') || buttonClass.includes('renter')) {
                    buttonType = 'renter';
                }
            } else if (buttonText.includes('edit') || buttonText.includes('update') || buttonText.includes('modify')) {
                action = 'edit';
                if (buttonText.includes('financial') || buttonId.includes('financial') || buttonClass.includes('financial')) {
                    buttonType = 'financial';
                } else if (buttonText.includes('member') || buttonId.includes('member') || buttonClass.includes('member')) {
                    buttonType = 'member';
                } else if (buttonText.includes('user') || buttonId.includes('user') || buttonClass.includes('user')) {
                    buttonType = 'user';
                } else if (buttonText.includes('plot') || buttonId.includes('plot') || buttonClass.includes('plot')) {
                    buttonType = 'plot';
                } else if (buttonText.includes('renter') || buttonId.includes('renter') || buttonClass.includes('renter')) {
                    buttonType = 'renter';
                } else if (buttonText.includes('announcement') || buttonId.includes('announcement') || buttonClass.includes('announcement')) {
                    buttonType = 'announcement';
                }
            } else if (buttonText.includes('delete') || buttonText.includes('remove')) {
                action = 'delete';
                if (buttonText.includes('financial') || buttonId.includes('financial') || buttonClass.includes('financial')) {
                    buttonType = 'financial';
                } else if (buttonText.includes('member') || buttonId.includes('member') || buttonClass.includes('member')) {
                    buttonType = 'member';
                } else if (buttonText.includes('user') || buttonId.includes('user') || buttonClass.includes('user')) {
                    buttonType = 'user';
                } else if (buttonText.includes('plot') || buttonId.includes('plot') || buttonClass.includes('plot')) {
                    buttonType = 'plot';
                } else if (buttonText.includes('renter') || buttonId.includes('renter') || buttonClass.includes('renter')) {
                    buttonType = 'renter';
                } else if (buttonText.includes('announcement') || buttonId.includes('announcement') || buttonClass.includes('announcement')) {
                    buttonType = 'announcement';
                } else if (buttonText.includes('document') || buttonId.includes('document') || buttonClass.includes('document')) {
                    buttonType = 'document';
                }
            } else if (buttonText.includes('save')) {
                action = 'save';
                if (buttonText.includes('plot') || buttonId.includes('plot') || buttonClass.includes('plot')) {
                    buttonType = 'plot';
                }
            } else if (buttonText.includes('upload')) {
                action = 'upload';
                if (buttonText.includes('announcement') || buttonId.includes('announcement') || buttonClass.includes('announcement')) {
                    buttonType = 'announcement';
                } else if (buttonText.includes('document') || buttonId.includes('document') || buttonClass.includes('document')) {
                    buttonType = 'document';
                }
            }
            
            // Check if this button should be restricted
            if (buttonType && action) {
                shouldRestrict = !hasButtonPermission(buttonType, action, userRole);
            }
            
            // Apply or remove restriction
            if (shouldRestrict) {
                button.classList.add('button-restricted');
                button.disabled = true;
                button.onclick = function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log(`Admin Button Restrictions: Blocked ${action} ${buttonType} button for role ${userRole}`);
                    return false;
                };
                restrictedCount++;
                console.log(`Admin Button Restrictions: Restricted "${buttonText}" button (${buttonType}-${action})`);
            } else {
                button.classList.remove('button-restricted');
                if (button.disabled && button.classList.contains('button-restricted-disabled')) {
                    button.disabled = false;
                }
                button.classList.remove('button-restricted-disabled');
            }
        });
        
        console.log(`Admin Button Restrictions: Applied restrictions to ${restrictedCount} buttons`);
        showRoleInfo(userRole, restrictedCount);
    }
    
    // Show role information badge
    function showRoleInfo(userRole, restrictedButtonCount) {
        // Remove existing badge
        const existingBadge = document.querySelector('.role-info-badge');
        if (existingBadge) {
            existingBadge.remove();
        }
        
        // Create new badge
        const badge = document.createElement('div');
        badge.className = 'role-info-badge';
        badge.innerHTML = `
            <div>Role: ${userRole.replace(/[_-]/g, ' ')}</div>
            <div style="font-size: 10px; opacity: 0.9;">
                ${restrictedButtonCount > 0 ? `${restrictedButtonCount} buttons restricted` : 'Full button access'}
            </div>
        `;
        
        document.body.appendChild(badge);
        
        // Auto-remove after 4 seconds
        setTimeout(() => {
            if (badge.parentNode) {
                badge.style.opacity = '0';
                setTimeout(() => {
                    if (badge.parentNode) {
                        badge.parentNode.removeChild(badge);
                    }
                }, 300);
            }
        }, 4000);
    }
    
    // Apply button restrictions when page loads or content changes
    async function applyAllButtonRestrictions() {
        if (restrictionsApplied) return;
        
        const userRole = await getUserRole();
        if (!userRole) return;
        
        console.log('Admin Button Restrictions: Applying button restrictions for role:', userRole);
        
        applyButtonRestrictions(userRole);
        restrictionsApplied = true;
    }
    
    // Initialize the button restriction system
    function initialize() {
        // Apply restrictions immediately if content is already loaded
        if (document.querySelectorAll('button, input[type="button"], input[type="submit"], .btn').length > 0) {
            applyAllButtonRestrictions();
        }
        
        // Set up mutation observer to catch dynamically loaded buttons
        if (typeof MutationObserver !== 'undefined') {
            const observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'childList') {
                        const hasNewButtons = Array.from(mutation.addedNodes).some(node => 
                            node.nodeType === 1 && 
                            (node.tagName === 'BUTTON' || 
                             node.querySelector && 
                             node.querySelector('button, input[type="button"], input[type="submit"], .btn'))
                        );
                        
                        if (hasNewButtons) {
                            restrictionsApplied = false;
                            setTimeout(() => applyAllButtonRestrictions(), 100);
                        }
                    }
                });
            });
            
            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
        }
        
        // Also reapply when content changes
        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(() => applyAllButtonRestrictions(), 500);
        });
        
        // Re-apply restrictions when navigating between pages
        window.addEventListener('hashchange', function() {
            restrictionsApplied = false;
            setTimeout(() => applyAllButtonRestrictions(), 200);
        });
        
        // Re-apply when page becomes visible (tab switching)
        document.addEventListener('visibilitychange', function() {
            if (!document.hidden) {
                restrictionsApplied = false;
                setTimeout(() => applyAllButtonRestrictions(), 100);
            }
        });
    }
    
    // Expose function for manual triggering
    window.applyAdminButtonRestrictions = applyAllButtonRestrictions;
    window.adminButtonRestrictionsInitialized = true;
    
    // Start initialization
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }
    
    console.log('Admin Button Restrictions: System initialized and ready');
    
})();
