// Role-based permissions manager
class RolePermissions {
    constructor() {
        this.userRole = null;
        this.permissions = {
            // Plots permissions
            PLOTS_EDIT: ['PRESIDENT', 'GENERAL_SECRETARY'],
            
            // Member management permissions
            MANAGE_MEMBERS_EDIT: ['PRESIDENT', 'GENERAL_SECRETARY'],
            
            // Financials permissions - Only Finance Secretary
            FINANCIALS_ADD: ['FINANCE_SECRETARY'],
            
            // Announcements permissions
            ANNOUNCEMENTS_UPLOAD: ['INFORMATION_SECRETARY'],
            
            // President Console Access
            PRESIDENT_CONSOLE_ACCESS: ['PRESIDENT', 'GENERAL_SECRETARY', 'FINANCE_SECRETARY', 'INFORMATION_SECRETARY'],
            
            // Prayer Timer permissions (everyone can set)
            PRAYER_TIMER: ['PRESIDENT', 'GENERAL_SECRETARY', 'FINANCE_SECRETARY', 'INFORMATION_SECRETARY', 'SOCIETY_MEMBER', 'VICE_PRESIDENT']
        };
        this.initializeUserRole();
    }

    async initializeUserRole() {
        try {
            const response = await fetch('/api/auth/current-user', {
                credentials: 'include'
            });
            if (response.ok) {
                const userData = await response.json();
                // Normalize role to match our permission system
                this.userRole = userData.role.toUpperCase().replace('-', '_');
                console.log('User role normalized:', this.userRole);
                this.updateUIBasedOnRole();
                
                // Apply permissions immediately and repeatedly for dynamic content
                this.applyPermissionsRepeatedly();
            }
        } catch (error) {
            console.error('Failed to fetch user role:', error);
        }
    }

    applyPermissionsRepeatedly() {
        // Apply permissions immediately
        this.updateUIBasedOnRole();
        
        // Apply again after a short delay for dynamic content
        setTimeout(() => this.updateUIBasedOnRole(), 500);
        setTimeout(() => this.updateUIBasedOnRole(), 1000);
        setTimeout(() => this.updateUIBasedOnRole(), 2000);
        
        // Set up mutation observer to watch for dynamic content changes
        if (typeof MutationObserver !== 'undefined') {
            const observer = new MutationObserver(() => {
                this.updateUIBasedOnRole();
            });
            
            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
        }
    }

    hasPermission(permission) {
        if (!this.userRole || !this.permissions[permission]) {
            return false;
        }
        return this.permissions[permission].includes(this.userRole);
    }

    updateUIBasedOnRole() {
        // Update plots page buttons
        this.updatePlotsPermissions();
        
        // Update manage members page
        this.updateManageMembersPermissions();
        
        // Update financials page
        this.updateFinancialsPermissions();
        
        // Update announcements page
        this.updateAnnouncementsPermissions();
        
        // Update prayer timer permissions
        this.updatePrayerTimerPermissions();
    }

    updatePlotsPermissions() {
        const plotsEditButtons = document.querySelectorAll('.edit-plot-btn, .save-plot-btn, .delete-plot-btn');
        const canEdit = this.hasPermission('PLOTS_EDIT');
        
        plotsEditButtons.forEach(button => {
            if (!canEdit) {
                button.disabled = true;
                button.style.opacity = '0.5';
                button.style.cursor = 'not-allowed';
                button.title = 'Only President and General Secretary can edit plots';
            }
        });
    }

    updateManageMembersPermissions() {
        const memberEditButtons = document.querySelectorAll('.edit-member-btn, .delete-member-btn, .add-member-btn, .edit-btn, .delete-btn');
        const canEdit = this.hasPermission('MANAGE_MEMBERS_EDIT');
        
        memberEditButtons.forEach(button => {
            if (!canEdit) {
                button.disabled = true;
                button.style.opacity = '0.5';
                button.style.cursor = 'not-allowed';
                button.title = 'Only President and General Secretary can edit/delete members';
            }
        });
    }

    updateFinancialsPermissions() {
        const financialButtons = document.querySelectorAll('.add-financial-btn, .edit-financial-btn, .delete-financial-btn, .edit-btn, .delete-btn, .new-btn');
        const canManageFinancials = this.hasPermission('FINANCIALS_ADD');
        
        financialButtons.forEach(button => {
            // Only disable if this is on a financials page
            const isFinancialPage = window.location.pathname.includes('Financials') || 
                                   button.closest('[id*="financial"]') ||
                                   button.onclick && button.onclick.toString().includes('Record') ||
                                   button.getAttribute('onclick') && button.getAttribute('onclick').includes('Record');
            
            if (isFinancialPage && !canManageFinancials) {
                button.disabled = true;
                button.style.opacity = '0.5';
                button.style.cursor = 'not-allowed';
                button.style.pointerEvents = 'none';
                button.title = 'Only Finance Secretary can manage financial records';
                
                // Remove click handlers to prevent any action
                button.onclick = function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                };
            }
        });
        
        // Also disable form submissions on financial forms
        if (window.location.pathname.includes('Financials') && !canManageFinancials) {
            const forms = document.querySelectorAll('form');
            forms.forEach(form => {
                if (form.id && form.id.toLowerCase().includes('financial')) {
                    form.onsubmit = function(e) {
                        e.preventDefault();
                        return false;
                    };
                }
            });
        }
    }

    updateAnnouncementsPermissions() {
        const announcementUploadButtons = document.querySelectorAll('.upload-announcement-btn, .add-announcement-btn');
        const canUpload = this.hasPermission('ANNOUNCEMENTS_UPLOAD');
        
        announcementUploadButtons.forEach(button => {
            if (!canUpload) {
                button.disabled = true;
                button.style.opacity = '0.5';
                button.style.cursor = 'not-allowed';
                button.title = 'Only Information Secretary can upload announcements';
            }
        });
    }

    updatePrayerTimerPermissions() {
        // Prayer timer can be set by everyone, so no restrictions
        const prayerTimerButtons = document.querySelectorAll('.prayer-timer-btn');
        prayerTimerButtons.forEach(button => {
            button.disabled = false;
            button.style.opacity = '1';
            button.style.cursor = 'pointer';
        });
    }

    showRoleBasedContent() {
        // Show/hide sections based on role
        const roleElements = document.querySelectorAll('[data-role-required]');
        
        roleElements.forEach(element => {
            const requiredRoles = element.getAttribute('data-role-required').split(',');
            if (!requiredRoles.includes(this.userRole)) {
                element.style.display = 'none';
            }
        });
    }

    preventUnauthorizedActions(action, permission) {
        if (!this.hasPermission(permission)) {
            // Silently prevent the action - no alert for better UX
            console.log(`Access denied: ${action} requires ${permission} permission. User role: ${this.userRole}`);
            return false;
        }
        return true;
    }

    // Special method to disable financial buttons after dynamic content loads
    disableFinancialButtonsForNonFinanceSecretary() {
        console.log('Checking financial permissions for role:', this.userRole);
        
        if (this.userRole !== 'FINANCE_SECRETARY') {
            // Get all buttons on the page
            const allButtons = document.querySelectorAll('button, input[type="submit"], input[type="button"]');
            
            allButtons.forEach(button => {
                const buttonText = button.textContent.toLowerCase();
                const buttonClick = button.getAttribute('onclick') || '';
                const buttonId = button.id || '';
                const buttonClass = button.className || '';
                
                // Check if this button is related to financial operations
                const isFinancialButton = 
                    buttonText.includes('edit') || 
                    buttonText.includes('delete') || 
                    buttonText.includes('new') || 
                    buttonText.includes('add') ||
                    buttonText.includes('record') ||
                    buttonClick.includes('Record') || 
                    buttonClick.includes('Financial') ||
                    buttonClick.includes('edit') ||
                    buttonClick.includes('delete') ||
                    buttonClick.includes('showModal') ||
                    buttonId.includes('financial') ||
                    buttonClass.includes('financial') ||
                    buttonClass.includes('edit-btn') ||
                    buttonClass.includes('delete-btn') ||
                    buttonClass.includes('new-btn');
                
                // Only disable if we're on a financial page AND it's a financial button
                if (window.location.pathname.includes('Financials') && isFinancialButton) {
                    console.log('Disabling button:', buttonText, button);
                    
                    button.disabled = true;
                    button.style.opacity = '0.3';
                    button.style.cursor = 'not-allowed';
                    button.style.pointerEvents = 'none';
                    button.style.backgroundColor = '#cccccc';
                    button.title = 'Only Finance Secretary can manage financial records';
                    
                    // Override any existing click handlers completely
                    button.onclick = function(e) {
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                        console.log('Financial action blocked for user role:', this.userRole);
                        return false;
                    };
                    
                    // Also override with addEventListener
                    button.addEventListener('click', function(e) {
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                        return false;
                    }, true);
                }
            });
            
            // Also disable any forms that might submit financial data
            const forms = document.querySelectorAll('form');
            forms.forEach(form => {
                if (window.location.pathname.includes('Financials')) {
                    form.addEventListener('submit', function(e) {
                        e.preventDefault();
                        e.stopImmediatePropagation();
                        console.log('Form submission blocked for user role:', this.userRole);
                        return false;
                    }, true);
                }
            });
        }
    }
}

// Initialize role permissions
const rolePermissions = new RolePermissions();

// Make it globally available
window.rolePermissions = rolePermissions;

// Update UI when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Apply permissions immediately if role is already loaded
    if (rolePermissions.userRole) {
        rolePermissions.updateUIBasedOnRole();
    } else {
        // Wait for role to be fetched and try multiple times
        let attempts = 0;
        const maxAttempts = 10;
        const checkInterval = setInterval(() => {
            attempts++;
            if (rolePermissions.userRole || attempts >= maxAttempts) {
                rolePermissions.updateUIBasedOnRole();
                clearInterval(checkInterval);
            }
        }, 200);
    }
});

// Also apply permissions when page becomes visible (in case of navigation)
document.addEventListener('visibilitychange', () => {
    if (!document.hidden && rolePermissions.userRole) {
        setTimeout(() => rolePermissions.updateUIBasedOnRole(), 100);
    }
});
