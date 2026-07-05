/**
 * Role-Based Security Script - Universal Version
 * Disables buttons and operations based on user roles across all pages
 * Provides professional UI by hiding access denied errors
 */

(function() {
    'use strict';
    
    console.log('Role Security: Universal role-based security loading...');
    window.roleSecurityInitialized = true;
    
    // Add CSS for disabled elements
    const style = document.createElement('style');
    style.textContent = `
        .security-disabled {
            opacity: 0.3 !important;
            filter: grayscale(100%) !important;
            pointer-events: none !important;
            cursor: not-allowed !important;
            background-color: #f8f9fa !important;
            border: 1px solid #dee2e6 !important;
            color: #6c757d !important;
            position: relative !important;
        }
        .security-disabled::after {
            content: "DISABLED" !important;
            position: absolute !important;
            top: 50% !important;
            left: 50% !important;
            transform: translate(-50%, -50%) !important;
            background: rgba(220, 53, 69, 0.9) !important;
            color: white !important;
            padding: 2px 6px !important;
            border-radius: 3px !important;
            font-size: 10px !important;
            font-weight: bold !important;
            z-index: 1000 !important;
        }
    `;
    document.head.appendChild(style);
    
    let currentUserRole = null;
    let securityApplied = false;
    
    // Role-based permissions configuration
    const PERMISSIONS = {
        // Financial operations - Finance Secretary ONLY (President removed per requirements)
        FINANCIAL: {
            allowedRoles: ['Finance-Secretary', 'Finance_Secretary', 'FINANCE_SECRETARY'],
            pages: ['Financials-President Console.html'],
            operations: ['add', 'edit', 'delete', 'save', 'submit', 'new'],
            functions: ['showModal', 'editRecord', 'deleteRecord', 'handleFormSubmit'],
            selectors: [
                'button[onclick*="showModal"]',
                'button[onclick*="editRecord"]',
                'button[onclick*="deleteRecord"]',
                '.add-btn',
                '.edit-btn', 
                '.delete-btn',
                'input[type="submit"]',
                'button[type="submit"]',
                'button:contains("Add")',
                'button:contains("Edit")',
                'button:contains("Delete")',
                'button:contains("Save")',
                'button:contains("New")'
            ]
        },
        
        // Announcement operations - Information Secretary ONLY (President removed per requirements)
        ANNOUNCEMENTS: {
            allowedRoles: ['Information-Secretary', 'Information_Secretary', 'INFORMATION_SECRETARY'],
            pages: ['Announcement-President Console.html'],
            operations: ['add', 'create', 'new', 'post', 'submit', 'upload'],
            functions: ['handleAnnouncementSubmit', 'createAnnouncement', 'postAnnouncement', 'handleDocumentUpload'],
            selectors: [
                'button[onclick*="Announcement"]',
                'input[type="submit"]',
                'button[type="submit"]',
                'form[id*="announcement"] button',
                'form[id*="upload"] button',
                '.upload-announcement-btn',
                'button:contains("Post")',
                'button:contains("Upload")',
                'button:contains("Create")'
            ],
            // Exception: Prayer timing is allowed for everyone
            exceptions: ['prayer', 'timing']
        },
        
        // Member/User management - General Secretary & President ONLY
        MEMBER_MANAGEMENT: {
            allowedRoles: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            pages: ['Manage Member-President Console.html', 'Manage User-President Console.html'],
            operations: ['add', 'edit', 'delete', 'create', 'update', 'save'],
            functions: ['addUser', 'editUser', 'deleteUser', 'addMember', 'editMember', 'deleteMember'],
            selectors: [
                'button[onclick*="User"]',
                'button[onclick*="Member"]', 
                'button[onclick*="edit"]',
                'button[onclick*="delete"]',
                'button[onclick*="add"]',
                '.edit-btn',
                '.delete-btn',
                '.add-btn',
                'input[type="submit"]',
                'button[type="submit"]',
                'button:contains("Add")',
                'button:contains("Edit")',
                'button:contains("Delete")',
                'button:contains("Save")'
            ]
        },
        
        // Plot management - General Secretary & President ONLY
        PLOT_MANAGEMENT: {
            allowedRoles: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY', 'President', 'PRESIDENT'],
            pages: ['Plots.html'],
            operations: ['add', 'edit', 'delete', 'create', 'update', 'save'],
            functions: ['addPlot', 'editPlot', 'deletePlot', 'handleFormSubmit'],
            selectors: [
                'button[onclick*="plot"]',
                'button[onclick*="edit"]',
                'button[onclick*="delete"]',
                'button[onclick*="save"]',
                '.edit-btn',
                '.delete-btn', 
                '.save-btn',
                'input[type="submit"]',
                'button[type="submit"]',
                'button:contains("Edit")',
                'button:contains("Delete")',
                'button:contains("Save")'
            ]
        },

        // Renter Docs - General Secretary ONLY (President NOT included per requirements)
        RENTER_DOCS: {
            allowedRoles: ['General-Secretary', 'General_Secretary', 'GENERAL_SECRETARY'],
            pages: ['Renters Docs-President Console.html'],
            operations: ['add', 'edit', 'delete', 'create', 'update', 'save'],
            functions: ['addRenter', 'editRenter', 'deleteRenter'],
            selectors: [
                'button[onclick*="renter"]',
                'button[onclick*="add"]',
                'button[onclick*="edit"]',
                'button[onclick*="delete"]',
                '.add-btn',
                '.edit-btn',
                '.delete-btn',
                'input[type="submit"]',
                'button[type="submit"]',
                'button:contains("Add")',
                'button:contains("Edit")',
                'button:contains("Delete")'
            ]
        }
    };
    
    // Get user role
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
                console.log('Role Security: User role confirmed as:', currentUserRole);
                return currentUserRole;
            }
        } catch (error) {
            console.error('Role Security: Error fetching user role:', error);
        }
        return null;
    }
    
    // Normalize role for comparison
    function normalizeRole(role) {
        if (!role) return '';
        return role.toLowerCase().replace(/[_\s-]/g, '');
    }
    
    // Check if user has permission for a specific operation
    function hasPermission(permissionType, userRole) {
        if (!userRole || !PERMISSIONS[permissionType]) return false;
        
        const allowedRoles = PERMISSIONS[permissionType].allowedRoles;
        const normalizedUserRole = normalizeRole(userRole);
        
        return allowedRoles.some(allowedRole => 
            normalizeRole(allowedRole) === normalizedUserRole
        );
    }
    
    // Get current page name
    function getCurrentPage() {
        const pathname = window.location.pathname;
        return pathname.split('/').pop() || pathname;
    }
    
    // Get required roles for a permission type
    function getRequiredRoles(permissionType) {
        if (!PERMISSIONS[permissionType]) return 'authorized role';
        const roles = PERMISSIONS[permissionType].allowedRoles;
        return roles.map(role => role.replace(/[_-]/g, ' ')).join(' or ');
    }
    
    // Apply security restrictions for a specific permission type
    function applyPermissionRestrictions(permissionType, userRole) {
        const permission = PERMISSIONS[permissionType];
        const currentPage = getCurrentPage();
        
        // Check if current page needs this permission type
        const pageNeedsPermission = permission.pages.some(page => 
            currentPage.includes(page.replace('.html', '')) || 
            currentPage === page
        );
        
        if (!pageNeedsPermission) return;
        
        if (!hasPermission(permissionType, userRole)) {
            console.log(`Role Security: Applying ${permissionType} restrictions for role: ${userRole}`);
            
            let disabledCount = 0;
            
            // Disable buttons based on selectors
            permission.selectors.forEach(selector => {
                try {
                    document.querySelectorAll(selector).forEach(element => {
                        if (element.hasAttribute('data-security-disabled')) return;
                        
                        // Special exception: Prayer timer setting is allowed for everyone
                        const elementText = element.textContent.toLowerCase();
                        const elementId = element.id.toLowerCase();
                        const isPrayerTimer = elementText.includes('prayer') || elementText.includes('timing') || 
                                            elementId.includes('prayer') ||
                                            element.closest('#prayer-timing-container') || 
                                            element.closest('[id*="prayer"]') ||
                                            element.closest('form[id*="prayer"]') ||
                                            element.className.includes('prayer-timer-btn');
                        
                        if (isPrayerTimer) {
                            console.log('Role Security: Allowing prayer timer access for everyone');
                            return;
                        }
                        
                        disableElement(element, `${permissionType} access restricted - requires ${getRequiredRoles(permissionType)}`);
                        disabledCount++;
                    });
                } catch (error) {
                    console.warn('Role Security: Error with selector:', selector, error);
                }
            });
            
            // Find and disable buttons by text content
            const allButtons = document.querySelectorAll('button, input[type="submit"], input[type="button"], a[onclick]');
            allButtons.forEach(element => {
                if (element.hasAttribute('data-security-disabled')) return;
                
                const text = element.textContent.toLowerCase();
                const onclick = element.getAttribute('onclick') || '';
                
                // Special exception: Prayer timer setting is allowed for everyone
                const isPrayerTimer = text.includes('prayer') || text.includes('timing') || 
                                    element.id.toLowerCase().includes('prayer') ||
                                    element.closest('#prayer-timing-container') ||
                                    element.className.includes('prayer-timer-btn');
                
                if (isPrayerTimer) {
                    return; // Allow prayer timer access for everyone
                }
                
                const hasRestrictedOperation = permission.operations.some(op => 
                    text.includes(op.toLowerCase()) || onclick.toLowerCase().includes(op.toLowerCase())
                );
                
                if (hasRestrictedOperation) {
                    disableElement(element, `${permissionType} access restricted - requires ${getRequiredRoles(permissionType)}`);
                    disabledCount++;
                }
            });
            
            // Override restricted functions - COMPLETE blocking
            permission.functions.forEach(funcName => {
                if (window[funcName] && !window[funcName]._securityOverridden) {
                    const originalFunc = window[funcName];
                    window[funcName] = function(...args) {
                        console.log(`Role Security: BLOCKED function call: ${funcName} (access restricted)`);
                        // Don't show any message, just silently block
                        return false;
                    };
                    window[funcName]._originalFunction = originalFunc;
                    window[funcName]._securityOverridden = true;
                    console.log(`Role Security: Function ${funcName} completely blocked`);
                }
            });
            
            // AGGRESSIVE FINANCIAL BLOCKING - Block all financial functions regardless of name
            if (permissionType === 'FINANCIAL') {
                const financialFunctions = ['deleteRecord', 'editRecord', 'showModal', 'handleFormSubmit', 'addRecord', 'saveRecord'];
                financialFunctions.forEach(funcName => {
                    if (window[funcName] && !window[funcName]._securityOverridden) {
                        const originalFunc = window[funcName];
                        window[funcName] = function(...args) {
                            console.log(`Role Security: BLOCKED financial function: ${funcName} - Finance Secretary access required`);
                            // Completely silent - no errors, no alerts, no messages
                            return false;
                        };
                        window[funcName]._originalFunction = originalFunc;
                        window[funcName]._securityOverridden = true;
                        console.log(`Role Security: Financial function ${funcName} completely blocked for non-Finance Secretary`);
                    }
                });
            }
            
            // Block form submissions completely - NO errors shown
            document.querySelectorAll('form').forEach(form => {
                const formId = form.id.toLowerCase();
                const formAction = form.action.toLowerCase();
                
                // Check if this form is restricted
                const hasRestrictedAction = permission.operations.some(op => 
                    formAction.includes(op) || 
                    formId.includes(op) || 
                    formAction.includes(permissionType.toLowerCase())
                );
                
                if (hasRestrictedAction) {
                    // Remove all existing submit handlers
                    const newForm = form.cloneNode(true);
                    form.parentNode.replaceChild(newForm, form);
                    
                    // Block ALL submission methods
                    newForm.addEventListener('submit', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        console.log(`Role Security: BLOCKED form submission silently`);
                        return false;
                    }, true);
                    
                    newForm.onsubmit = function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        return false;
                    };
                    
                    // Disable all submit buttons in this form
                    newForm.querySelectorAll('input[type="submit"], button[type="submit"], button:not([type])').forEach(btn => {
                        disableElement(btn, `${permissionType} access restricted - requires ${getRequiredRoles(permissionType)}`);
                    });
                    
                    console.log(`Role Security: Form completely blocked - ${formId || 'unnamed form'}`);
                }
            });
            
            console.log(`Role Security: Applied ${permissionType} restrictions - disabled ${disabledCount} elements`);
        }
    }
    
    // Disable an element with professional styling - COMPLETE blocking
    function disableElement(element, reason) {
        // Mark as processed to avoid double processing
        element.setAttribute('data-security-disabled', 'true');
        
        // Completely disable the element
        element.disabled = true;
        element.setAttribute('disabled', 'true');
        
        // Apply visual styling to show it's disabled - CLEARLY DISABLED APPEARANCE
        element.style.cssText = `
            opacity: 0.4 !important;
            cursor: not-allowed !important;
            pointer-events: none !important;
            background-color: #f8f9fa !important;
            border: 2px solid #dee2e6 !important;
            color: #6c757d !important;
            box-shadow: none !important;
            filter: grayscale(100%) !important;
            text-decoration: line-through !important;
            position: relative !important;
        `;
        
        // Mark as completely disabled
        element.disabled = true;
        element.setAttribute('disabled', 'true');
        element.setAttribute('aria-disabled', 'true');
        element.setAttribute('tabindex', '-1');
        
        // Add CSS class for additional styling
        element.classList.add('security-disabled');
        
        // Force style application
        element.setAttribute('style', element.style.cssText);
        
        // Set helpful tooltip
        element.title = reason;
        
        // Remove ALL existing event listeners by cloning the element
        const newElement = element.cloneNode(true);
        element.parentNode.replaceChild(newElement, element);
        
        // Override ALL possible event handlers on the new element
        const eventTypes = ['click', 'mousedown', 'mouseup', 'touchstart', 'touchend', 'submit', 'focus', 'keydown', 'keyup'];
        eventTypes.forEach(eventType => {
            newElement.addEventListener(eventType, function(e) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                console.log(`Role Security: Blocked ${eventType} event on restricted element`);
                return false;
            }, true);
        });
        
        // Override onclick completely
        newElement.onclick = function(e) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            return false;
        };
        
        // Remove href for links
        if (newElement.tagName === 'A') {
            newElement.removeAttribute('href');
            newElement.setAttribute('href', 'javascript:void(0)');
        }
        
        // Override form submission if it's a form element
        if (newElement.tagName === 'FORM') {
            newElement.onsubmit = function(e) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            };
        }
        
            console.log(`Role Security: Element completely blocked - ${element.tagName}: "${element.textContent.trim()}" - Classes: ${element.className} - Applied transparent styling`);
    }
    
    // Apply all security restrictions
    async function applyAllSecurityRestrictions() {
        if (securityApplied) return;
        
        const userRole = await getUserRole();
        if (!userRole) return;
        
        console.log('Role Security: Applying all restrictions for role:', userRole);
        
        // Apply restrictions for each permission type
        Object.keys(PERMISSIONS).forEach(permissionType => {
            applyPermissionRestrictions(permissionType, userRole);
        });
        
        securityApplied = true;
        console.log('Role Security: All security restrictions applied - buttons are now non-pressable');
    }
    
    // Initialize security system with aggressive monitoring
    async function initialize() {
        await applyAllSecurityRestrictions();
        
        // IMMEDIATE BUTTON BLOCKING ON FINANCIAL PAGE
        const userRole = await getUserRole();
        if (userRole && userRole !== 'Finance-Secretary' && userRole !== 'Finance_Secretary' && userRole !== 'FINANCE_SECRETARY') {
            const isOnFinancialPage = window.location.pathname.includes('Financials-President Console.html');
            if (isOnFinancialPage) {
                // FORCE disable ALL Edit/Delete buttons immediately and repeatedly
                const forceDisableFinancialButtons = () => {
                    const editButtons = document.querySelectorAll('.edit-btn, .edit-financial-btn, button[onclick*="editRecord"]');
                    const deleteButtons = document.querySelectorAll('.delete-btn, .delete-financial-btn, button[onclick*="deleteRecord"]');
                    
                    [...editButtons, ...deleteButtons].forEach(btn => {
                        if (!btn.hasAttribute('data-security-disabled')) {
                            disableElement(btn, 'Finance Secretary access required');
                            console.log('Role Security: FORCE DISABLED financial button:', btn.textContent.trim());
                        }
                    });
                };
                
                // Run immediately and then every 100ms for first 5 seconds
                forceDisableFinancialButtons();
                let forceCount = 0;
                const forceInterval = setInterval(() => {
                    forceDisableFinancialButtons();
                    forceCount++;
                    if (forceCount >= 50) { // 50 times over 5 seconds
                        clearInterval(forceInterval);
                        console.log('Role Security: Stopped force disabling');
                    }
                }, 100);
            }
        }
        
        // AGGRESSIVE FINANCIAL FUNCTION OVERRIDE - Force override every 500ms for first 10 seconds
        let overrideCount = 0;
        const maxOverrides = 20; // 20 times over 10 seconds
        
        const aggressiveOverride = setInterval(async function() {
            overrideCount++;
            const userRole = await getUserRole();
            
            if (userRole && userRole !== 'Finance-Secretary' && userRole !== 'Finance_Secretary' && userRole !== 'FINANCE_SECRETARY') {
                const isOnFinancialPage = window.location.pathname.includes('Financials-President Console.html');
                if (isOnFinancialPage) {
                    // Force override financial functions
                    const financialFunctions = ['deleteRecord', 'editRecord', 'showModal', 'handleFormSubmit'];
                    financialFunctions.forEach(funcName => {
                        if (window[funcName]) {
                            window[funcName] = function(...args) {
                                console.log(`Role Security: AGGRESSIVELY BLOCKED ${funcName} - Finance Secretary required`);
                                return false; // Silent blocking
                            };
                            console.log(`Role Security: AGGRESSIVE override applied to ${funcName} (attempt ${overrideCount})`);
                        }
                    });
                }
            }
            
            if (overrideCount >= maxOverrides) {
                clearInterval(aggressiveOverride);
                console.log('Role Security: Stopped aggressive function override');
            }
        }, 500); // Every 500ms
        
        // Set up mutation observer for dynamic content - AGGRESSIVE monitoring
        if (typeof MutationObserver !== 'undefined') {
            const observer = new MutationObserver(function(mutations) {
                let shouldReapply = false;
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                        // Check if new buttons were added
                        mutation.addedNodes.forEach(node => {
                            if (node.nodeType === 1) { // Element node
                                const newButtons = node.querySelectorAll ? node.querySelectorAll('button, input[type="submit"], input[type="button"], a[onclick]') : [];
                                if (newButtons.length > 0 || node.tagName === 'BUTTON' || node.tagName === 'INPUT') {
                                    shouldReapply = true;
                                    
                                    // IMMEDIATE blocking of financial buttons if user is not Finance Secretary
                                    if (currentUserRole && currentUserRole !== 'Finance-Secretary' && currentUserRole !== 'Finance_Secretary' && currentUserRole !== 'FINANCE_SECRETARY') {
                                        const isOnFinancialPage = window.location.pathname.includes('Financials-President Console.html');
                                        if (isOnFinancialPage) {
                                            // Immediately disable any financial buttons that were just added
                                            const financialButtons = node.querySelectorAll ? 
                                                node.querySelectorAll('.delete-btn, .edit-btn, .delete-financial-btn, .edit-financial-btn, button[onclick*="deleteRecord"], button[onclick*="editRecord"]') : [];
                                            
                                            financialButtons.forEach(btn => {
                                                disableElement(btn, 'Financial access restricted - requires Finance Secretary');
                                                console.log('Role Security: IMMEDIATELY disabled new financial button');
                                            });
                                            
                                            // Also check if the node itself is a financial button
                                            if (node.classList && (node.classList.contains('delete-btn') || node.classList.contains('edit-btn') || 
                                                node.classList.contains('delete-financial-btn') || node.classList.contains('edit-financial-btn'))) {
                                                disableElement(node, 'Financial access restricted - requires Finance Secretary');
                                                console.log('Role Security: IMMEDIATELY disabled new financial button node');
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                    
                    // Also watch for attribute changes that might re-enable buttons
                    if (mutation.type === 'attributes' && 
                        (mutation.attributeName === 'disabled' || mutation.attributeName === 'onclick')) {
                        shouldReapply = true;
                    }
                });
                
                if (shouldReapply) {
                    setTimeout(() => {
                        securityApplied = false; // Reset to allow reapplication
                        applyAllSecurityRestrictions();
                    }, 10); // Very quick reapplication
                }
            });
            
            observer.observe(document.body, {
                childList: true,
                subtree: true,
                attributes: true,
                attributeFilter: ['disabled', 'onclick', 'style', 'class']
            });
            
            console.log('Role Security: Aggressive monitoring enabled - will catch ALL new buttons');
        }
        
        // Periodic reapplication - MORE frequent for better coverage
        let reapplicationCount = 0;
        const reapplyInterval = setInterval(function() {
            reapplicationCount++;
            if (reapplicationCount < 20) { // Run 20 times over 40 seconds
                securityApplied = false;
                applyAllSecurityRestrictions();
            } else {
                clearInterval(reapplyInterval);
                console.log('Role Security: Stopped periodic reapplication');
            }
        }, 2000);
        
        // Also reapply when user interacts with the page
        ['click', 'focus', 'keydown'].forEach(eventType => {
            document.addEventListener(eventType, function() {
                if (!securityApplied) {
                    setTimeout(() => applyAllSecurityRestrictions(), 100);
                }
            }, true);
        });
    }
    
    // Start initialization
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }
    
    // ADDITIONAL AGGRESSIVE MONITORING - Check every 2 seconds for first 30 seconds
    let aggressiveCheckCount = 0;
    const aggressiveCheck = setInterval(async function() {
        aggressiveCheckCount++;
        const userRole = await getUserRole();
        
        if (userRole && userRole !== 'Finance-Secretary' && userRole !== 'Finance_Secretary' && userRole !== 'FINANCE_SECRETARY') {
            const isOnFinancialPage = window.location.pathname.includes('Financials-President Console.html');
            if (isOnFinancialPage) {
                // Find and disable any Edit/Delete buttons that might have been missed
                const allButtons = document.querySelectorAll('button, .edit-btn, .delete-btn, .edit-financial-btn, .delete-financial-btn');
                
                allButtons.forEach(btn => {
                    const text = btn.textContent.toLowerCase();
                    const onclick = btn.getAttribute('onclick') || '';
                    const classes = btn.className.toLowerCase();
                    
                    const isEditButton = text.includes('edit') || onclick.includes('editRecord') || classes.includes('edit');
                    const isDeleteButton = text.includes('delete') || onclick.includes('deleteRecord') || classes.includes('delete');
                    
                    if ((isEditButton || isDeleteButton) && !btn.hasAttribute('data-security-disabled')) {
                        disableElement(btn, 'Finance Secretary access required');
                        console.log(`Role Security: AGGRESSIVE CHECK ${aggressiveCheckCount} - Disabled button:`, btn.textContent.trim());
                    }
                });
            }
        }
        
        if (aggressiveCheckCount >= 15) { // 15 times over 30 seconds
            clearInterval(aggressiveCheck);
            console.log('Role Security: Stopped aggressive checking');
        }
    }, 2000);
    
    console.log('Role Security: Universal security system initialized');
    
    // Global event interceptor - catch ANY click on restricted elements
    document.addEventListener('click', function(e) {
        const target = e.target;
        
        // If element is marked as security-disabled, BLOCK everything
        if (target.hasAttribute && target.hasAttribute('data-security-disabled')) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            console.log('Role Security: INTERCEPTED click on disabled element');
            return false;
        }
        
        // AGGRESSIVE CHECK: Block financial operations for non-Finance Secretary
        if (currentUserRole && currentUserRole !== 'Finance-Secretary' && currentUserRole !== 'Finance_Secretary' && currentUserRole !== 'FINANCE_SECRETARY') {
            const isFinancialButton = target.classList.contains('delete-btn') || 
                                    target.classList.contains('edit-btn') ||
                                    target.classList.contains('delete-financial-btn') ||
                                    target.classList.contains('edit-financial-btn') ||
                                    target.textContent.toLowerCase().includes('delete') ||
                                    target.textContent.toLowerCase().includes('edit') ||
                                    (target.getAttribute('onclick') && (
                                        target.getAttribute('onclick').includes('deleteRecord') ||
                                        target.getAttribute('onclick').includes('editRecord') ||
                                        target.getAttribute('onclick').includes('showModal')
                                    ));
                                    
            const isOnFinancialPage = window.location.pathname.includes('Financials-President Console.html');
            
            if (isFinancialButton && isOnFinancialPage) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                console.log('Role Security: BLOCKED financial operation - Finance Secretary access required');
                return false;
            }
        }
        
        // Also check parent elements
        let parent = target.parentElement;
        while (parent) {
            if (parent.hasAttribute && parent.hasAttribute('data-security-disabled')) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                console.log('Role Security: INTERCEPTED click on child of disabled element');
                return false;
            }
            parent = parent.parentElement;
        }
    }, true); // Use capture phase to catch early
    
    // Global form submission interceptor
    document.addEventListener('submit', function(e) {
        const form = e.target;
        if (form.hasAttribute && form.hasAttribute('data-security-disabled')) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            console.log('Role Security: INTERCEPTED form submission');
            return false;
        }
    }, true);
    
})();
