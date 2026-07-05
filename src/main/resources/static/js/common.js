// Common JavaScript functions used across multiple pages

// --- IMAGE CACHING SYSTEM ---
const imageCache = new Map();
let profileImageCache = null;

function getCachedImage(imageUrl) {
    return imageCache.get(imageUrl);
}

function setCachedImage(imageUrl, imageElement) {
    imageCache.set(imageUrl, imageElement);
}

function preloadImage(imageUrl) {
    return new Promise((resolve, reject) => {
        // Check if already cached
        if (imageCache.has(imageUrl)) {
            resolve(imageCache.get(imageUrl));
            return;
        }

        const img = new Image();
        img.onload = function() {
            setCachedImage(imageUrl, imageUrl);
            resolve(imageUrl);
        };
        img.onerror = function() {
            reject(new Error(`Failed to load image: ${imageUrl}`));
        };
        img.src = imageUrl;
    });
}

// --- FLOATING NOTIFICATION SYSTEM ---
function showFloatingNotification(message, type = 'success', duration = 4000) {
    // Remove any existing notifications
    const existingNotification = document.getElementById('floatingNotification');
    if (existingNotification) {
        existingNotification.remove();
    }

    // Create notification element
    const notification = document.createElement('div');
    notification.id = 'floatingNotification';
    notification.innerHTML = `
        <div class="floating-notification-content">
            <div class="floating-notification-icon">
                ${type === 'success' ? '✅' : '❌'}
            </div>
            <div class="floating-notification-message">${message}</div>
            <button class="floating-notification-close" onclick="closeFloatingNotification()">×</button>
        </div>
    `;

    // Add CSS styles
    notification.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        z-index: 10000;
        background: ${type === 'success' ? 'linear-gradient(135deg, #27ae60, #2ecc71)' : 'linear-gradient(135deg, #e74c3c, #c0392b)'};
        color: white;
        padding: 0;
        border-radius: 15px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        min-width: 350px;
        max-width: 90vw;
        animation: floatingNotificationSlideIn 0.5s ease-out;
        font-family: 'Poppins', sans-serif;
        border: 3px solid ${type === 'success' ? '#1e8449' : '#922b21'};
    `;

    // Add CSS for content styling
    const contentStyle = `
        .floating-notification-content {
            display: flex;
            align-items: center;
            padding: 20px;
            gap: 15px;
        }
        .floating-notification-icon {
            font-size: 24px;
            min-width: 30px;
            text-align: center;
        }
        .floating-notification-message {
            flex: 1;
            font-size: 16px;
            font-weight: 500;
            line-height: 1.4;
        }
        .floating-notification-close {
            background: rgba(255,255,255,0.2);
            border: none;
            color: white;
            font-size: 18px;
            width: 25px;
            height: 25px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background 0.3s ease;
        }
        .floating-notification-close:hover {
            background: rgba(255,255,255,0.3);
        }
        @keyframes floatingNotificationSlideIn {
            from {
                opacity: 0;
                transform: translate(-50%, -60%) scale(0.8);
            }
            to {
                opacity: 1;
                transform: translate(-50%, -50%) scale(1);
            }
        }
        @keyframes floatingNotificationSlideOut {
            from {
                opacity: 1;
                transform: translate(-50%, -50%) scale(1);
            }
            to {
                opacity: 0;
                transform: translate(-50%, -40%) scale(0.8);
            }
        }
    `;

    // Add styles to head if not already present
    if (!document.getElementById('floatingNotificationStyles')) {
        const styleElement = document.createElement('style');
        styleElement.id = 'floatingNotificationStyles';
        styleElement.textContent = contentStyle;
        document.head.appendChild(styleElement);
    }

    // Add to body
    document.body.appendChild(notification);

    // Auto-remove after duration
    setTimeout(() => {
        closeFloatingNotification();
    }, duration);
}

function closeFloatingNotification() {
    const notification = document.getElementById('floatingNotification');
    if (notification) {
        notification.style.animation = 'floatingNotificationSlideOut 0.3s ease-in';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 300);
    }
}

// --- SUCCESS/ERROR HELPER FUNCTIONS ---
function showSuccess(message, duration = 4000) {
    showFloatingNotification(message, 'success', duration);
}

function showError(message, duration = 6000) {
    showFloatingNotification(message, 'error', duration);
}

// Global logout function - needed because header.html is loaded dynamically
function handleLogout() {
    // Clear the session storage so the popup appears on next login
    sessionStorage.removeItem('popupShown');
    // Clear the announcement popup session storage
    sessionStorage.removeItem('jeewan_announcement_shown');
    // Clear the Masjid secret code validation
    sessionStorage.removeItem('masjidCodeValidated');
    // Clear the Charity secret code validation
    sessionStorage.removeItem('charityCodeValidated');
    
    // Create and submit a logout form
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';
    
    // Add CSRF token if needed (Spring Security usually handles this automatically)
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    
    if (csrfToken && csrfHeader) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken.getAttribute('content');
        form.appendChild(csrfInput);
    }
    
    document.body.appendChild(form);
    form.submit();
}

// Function to set active menu item
function setActive() {
    let currentPage = window.location.pathname;
    let menuItems = document.querySelectorAll('.menu-block');
    menuItems.forEach(item => {
        if (currentPage.includes(item.href.split("/").pop())) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

// Initialize common functionality
document.addEventListener('DOMContentLoaded', function() {
    // Set active menu item after DOM is loaded  
    setTimeout(setActive, 100); // Small delay to ensure header is loaded
    // Load user profile image after DOM is loaded
    setTimeout(loadUserProfileImage, 200); // Load after header
    
    // For pages that dynamically load header, try again after a longer delay
    setTimeout(function() {
        if (document.getElementById('userProfileImage') && !document.getElementById('userProfileImage').src) {
            loadUserProfileImage();
        }
    }, 1000);
    
    // Enforce feedback requirement after page loads
    setTimeout(enforceFeedbackRequirement, 500);
});

// Function to get profile image path with correct URL construction
function getProfileImagePath(profilePicturePath) {
    console.log('🛠️ Processing profile picture path:', profilePicturePath);
    
    if (!profilePicturePath || profilePicturePath.trim() === '') {
        console.log('📋 No profile path provided, using default');
        return '/image/default-avatar.png';
    }
    
    // Clean the path
    const cleanPath = profilePicturePath.trim();
    
    // If path already starts with http:// or https://, use as-is
    if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://')) {
        console.log('🌐 Full URL provided:', cleanPath);
        return cleanPath;
    }
    
    // If path already starts with /, use as-is
    if (cleanPath.startsWith('/')) {
        console.log('🗂️ Absolute path provided:', cleanPath);
        return cleanPath;
    }
    
    // If path starts with "profiles/", add "/uploads/" prefix
    if (cleanPath.startsWith('profiles/')) {
        const result = '/uploads/' + cleanPath;
        console.log('📁 Profiles path detected, final path:', result);
        return result;
    }
    
    // If it's just a filename, assume it's in uploads/profiles/
    if (!cleanPath.includes('/')) {
        const result = '/uploads/profiles/' + cleanPath;
        console.log('📄 Filename detected, final path:', result);
        return result;
    }
    
    // Default case - add leading slash if not present
    const result = cleanPath.startsWith('/') ? cleanPath : '/' + cleanPath;
    console.log('🔧 Default case, final path:', result);
    return result;
}

// Function to load current user's profile image
async function loadUserProfileImage() {
    console.log('🖼️ Loading user profile image...');
    
    // Check if profile image is already cached in this session
    if (profileImageCache) {
        console.log('✨ Using cached profile image');
        const profileImg = document.getElementById('userProfileImage');
        if (profileImg) {
            profileImg.src = profileImageCache;
            profileImg.style.display = 'block';
        }
        return;
    }
    
    try {
        const response = await fetch('/api/users/me');
        console.log('📡 API Response status:', response.status);
        
        const profileImg = document.getElementById('userProfileImage');
        if (!profileImg) {
            console.log('⚠️ Profile image element not found');
            return;
        }
        
        if (response.ok) {
            const user = await response.json();
            currentUserData = user; // Cache user data for role checking
            console.log('👤 User data:', user);
            console.log('🎯 Profile picture path:', user.profilePicturePath);
            
            if (user.profilePicturePath && user.profilePicturePath.trim() !== '') {
                const imagePath = getProfileImagePath(user.profilePicturePath);
                console.log('🔗 Final image path:', imagePath);
                
                // Preload and cache the image
                try {
                    await preloadImage(imagePath);
                    console.log('✅ User image loaded and cached:', imagePath);
                    profileImg.src = imagePath;
                    profileImg.style.display = 'block';
                    profileImageCache = imagePath; // Cache for next page loads
                } catch (error) {
                    console.error('❌ Failed to load user image:', imagePath);
                    // Show default avatar only if user image fails
                    const defaultPath = '/image/default-avatar.png';
                    profileImg.src = defaultPath;
                    profileImg.style.display = 'block';
                    profileImageCache = defaultPath;
                }
            } else {
                console.log('📋 No profile picture set, showing default avatar');
                const defaultPath = '/image/default-avatar.png';
                profileImg.src = defaultPath;
                profileImg.style.display = 'block';
                profileImageCache = defaultPath;
            }
            
            // Set up role-based UI after user data is loaded
            setTimeout(setupRoleBasedUI, 100);
            
        } else {
            console.log('🔒 User not logged in, showing default avatar');
            const defaultPath = '/image/default-avatar.png';
            profileImg.src = defaultPath;
            profileImg.style.display = 'block';
            profileImageCache = defaultPath;
        }
    } catch (error) {
        console.error('💥 Error loading user profile image:', error);
        const profileImg = document.getElementById('userProfileImage');
        if (profileImg) {
            const defaultPath = '/image/default-avatar.png';
            profileImg.src = defaultPath;
            profileImg.style.display = 'block';
            profileImageCache = defaultPath;
        }
    }
}

// Role-based access control functions
let currentUserData = null;

// Function to get current user data
async function getCurrentUser() {
    if (currentUserData) {
        return currentUserData;
    }
    
    try {
        const response = await fetch('/api/users/me');
        if (!response.ok) {
            throw new Error('User not authenticated');
        }
        currentUserData = await response.json();
        console.log('🔐 Current user loaded:', currentUserData);
        return currentUserData;
    } catch (error) {
        console.error('❌ Error fetching current user:', error);
        throw error;
    }
}

// Function to check if user has required role(s)
async function hasRole(requiredRoles) {
    try {
        const user = await getCurrentUser();
        const userRole = user.role;
        
        // Convert single role to array for consistent checking
        const rolesArray = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
        
        console.log('🔍 Checking role access:', {
            userRole: userRole,
            requiredRoles: rolesArray,
            hasAccess: rolesArray.includes(userRole)
        });
        
        return rolesArray.includes(userRole);
    } catch (error) {
        console.error('❌ Error checking user role:', error);
        return false;
    }
}

// Function to check if user is admin (President or high-level roles)
async function isAdmin() {
    const adminRoles = ['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary'];
    return await hasRole(adminRoles);
}

// Function to check if user can access President Console
async function canAccessPresidentConsole() {
    const presidentRoles = ['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary'];
    return await hasRole(presidentRoles);
}

// Function to redirect unauthorized users
function redirectToUnauthorized() {
    console.log('🚫 Unauthorized access, redirecting to home');
    alert('Access Denied: You do not have permission to access this page.');
    window.location.href = '/home.html';
}

// Enhanced page access check with view-only mode
async function checkPageAccess(requiredRoles, viewOnlyRoles = []) {
    try {
        const currentUser = await getCurrentUser();
        if (!currentUser) {
            // Redirect to login page
            alert('Please log in to access this page.');
            window.location.href = '/Login Page.html';
            return { hasAccess: false, isViewOnly: false, userRole: null };
        }

        const userRole = currentUser.role;
        
        // Check if user has full access
        if (requiredRoles.includes(userRole)) {
            return { hasAccess: true, isViewOnly: false, userRole: userRole };
        }
        
        // Check if user has view-only access
        if (viewOnlyRoles.includes(userRole)) {
            return { hasAccess: true, isViewOnly: true, userRole: userRole };
        }
        
        // No access at all
        redirectToUnauthorized();
        return { hasAccess: false, isViewOnly: false, userRole: userRole };
    } catch (error) {
        console.error('❌ Error checking page access:', error);
        // If there's an error (like user not logged in), redirect to login
        alert('Please log in to access this page.');
        window.location.href = '/Login Page.html';
        return { hasAccess: false, isViewOnly: false, userRole: null };
    }
}

// Function to disable editing for view-only mode
function setViewOnlyMode() {
    // Disable all buttons except close/back buttons
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
        const buttonText = button.textContent.toLowerCase();
        if (!buttonText.includes('close') && 
            !buttonText.includes('back') && 
            !buttonText.includes('cancel') &&
            !buttonText.includes('search')) {
            button.disabled = true;
            button.style.opacity = '0.5';
            button.style.cursor = 'not-allowed';
            button.title = 'View-only mode: You can view but not edit';
        }
    });

    // Disable all input fields
    const inputs = document.querySelectorAll('input:not([type="search"]), select, textarea');
    inputs.forEach(input => {
        input.disabled = true;
        input.style.opacity = '0.7';
        input.title = 'View-only mode: You can view but not edit';
    });

    // Add view-only banner
    const banner = document.createElement('div');
    banner.innerHTML = `
        <div style="background: #f39c12; color: white; padding: 10px; text-align: center; font-weight: bold; position: fixed; top: 0; left: 0; right: 0; z-index: 9999;">
            📖 VIEW-ONLY MODE: You can view this information but cannot make changes
        </div>
    `;
    document.body.insertBefore(banner, document.body.firstChild);
    
    // Add margin to body to account for banner
    document.body.style.marginTop = '50px';
}

// Function to hide/show elements based on role
async function setupRoleBasedUI() {
    try {
        const user = await getCurrentUser();
        const userRole = user.role;
        
        // Hide President Console link for non-admin users
        const presidentConsoleLink = document.getElementById('president-console');
        if (presidentConsoleLink) {
            const canAccess = await canAccessPresidentConsole();
            if (!canAccess) {
                presidentConsoleLink.style.display = 'none';
                console.log('🔒 President Console link hidden for role:', userRole);
            }
        }
        
        console.log('🎯 Role-based UI setup completed for user:', userRole);
    } catch (error) {
        console.error('❌ Error setting up role-based UI:', error);
    }
}

// Granular permission functions for specific President Console sections
async function canManageUsers() {
    // Only President and General-Secretary can manage users
    return await hasRole(['President', 'General-Secretary']);
}

async function canViewUsers() {
    // All admin roles can view users, but only some can edit
    return await hasRole(['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary']);
}

async function canManageMembers() {
    // President, General-Secretary, and Information-Secretary can manage staff members
    return await hasRole(['President', 'General-Secretary', 'Information-Secretary']);
}

async function canViewMembers() {
    // All admin roles can view members
    return await hasRole(['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary']);
}

async function canManageFinancials() {
    // President, Finance-Secretary, and General-Secretary can manage finances
    return await hasRole(['President', 'Finance-Secretary', 'General-Secretary']);
}

async function canViewFinancials() {
    // All admin roles can view financials
    return await hasRole(['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary']);
}

async function canManageAnnouncements() {
    // President, Information-Secretary, and General-Secretary can manage announcements
    return await hasRole(['President', 'Information-Secretary', 'General-Secretary']);
}

async function canViewAnnouncements() {
    // All admin roles can view announcements
    return await hasRole(['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary']);
}

async function canReviewFeedback() {
    // President, General-Secretary, and Information-Secretary can review feedback
    return await hasRole(['President', 'General-Secretary', 'Information-Secretary']);
}

async function canViewFeedback() {
    // All admin roles can view feedback
    return await hasRole(['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary']);
}

async function canManageRenters() {
    // President and General-Secretary can manage renter documents
    return await hasRole(['President', 'General-Secretary']);
}

async function canViewRenters() {
    // All admin roles can view renter documents
    return await hasRole(['President', 'Vice-President', 'General-Secretary', 'Finance-Secretary', 'Information-Secretary']);
}

// Function to setup role-specific President Console sections
async function setupPresidentConsoleAccess() {
    try {
        const user = await getCurrentUser();
        console.log('🔐 Setting up President Console access for role:', user.role);
        
        // Hide/show sections based on specific permissions
        const sections = [
            { selector: 'a[href="Manage User-President Console.html"]', permission: await canManageUsers() },
            { selector: 'a[href="Manage Member-President Console.html"]', permission: await canManageMembers() },
            { selector: 'a[href="Financials-President Console.html"]', permission: await canManageFinancials() },
            { selector: 'a[href="Announcement-President Console.html"]', permission: await canManageAnnouncements() },
            { selector: 'a[href="Review-President Console.html"]', permission: await canReviewFeedback() },
            { selector: 'a[href="Renters Docs-President Console.html"]', permission: await canManageRenters() }
        ];
        
        sections.forEach(section => {
            const element = document.querySelector(section.selector);
            if (element) {
                if (!section.permission) {
                    element.style.display = 'none';
                    console.log('🔒 Hidden section:', element.href);
                } else {
                    element.style.display = 'block';
                    console.log('✅ Visible section:', element.href);
                }
            }
        });
        
    } catch (error) {
        console.error('❌ Error setting up President Console access:', error);
    }
}

// Monthly Feedback Access Control Functions
async function checkFeedbackRequirement() {
    try {
        const response = await fetch('/api/feedback/check-required');
        if (!response.ok) {
            throw new Error('Failed to check feedback requirement');
        }
        const result = await response.json();
        console.log('📝 Feedback requirement check:', result);
        return result;
    } catch (error) {
        console.error('❌ Error checking feedback requirement:', error);
        return { isRequired: false, message: 'Unable to check feedback status' };
    }
}

async function checkPortalAccess() {
    try {
        const response = await fetch('/api/access/check-portal-access');
        if (!response.ok) {
            throw new Error('Failed to check portal access');
        }
        const result = await response.json();
        console.log('🚪 Portal access check:', result);
        return result;
    } catch (error) {
        console.error('❌ Error checking portal access:', error);
        return { canAccess: false, reason: 'UNKNOWN_ERROR' };
    }
}

// Function to show mandatory feedback modal
function showMandatoryFeedbackModal(message) {
    // Create modal HTML
    const modalHTML = `
        <div id="mandatory-feedback-modal" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.8); z-index: 10000; display: flex; justify-content: center; align-items: center;">
            <div style="background: white; padding: 30px; border-radius: 10px; max-width: 500px; text-align: center; box-shadow: 0 8px 32px rgba(0,0,0,0.3);">
                <h2 style="color: #e74c3c; margin-bottom: 20px;">📝 Monthly Feedback Required</h2>
                <p style="margin-bottom: 25px; font-size: 16px; line-height: 1.6;">${message}</p>
                <p style="margin-bottom: 25px; color: #7f8c8d;">You must complete the monthly feedback form to continue using the portal.</p>
                <button onclick="goToFeedbackPage()" style="background: #3498db; color: white; border: none; padding: 12px 25px; border-radius: 5px; font-size: 16px; cursor: pointer; margin-right: 10px;">Complete Feedback Now</button>
                <button onclick="logoutUser()" style="background: #95a5a6; color: white; border: none; padding: 12px 25px; border-radius: 5px; font-size: 16px; cursor: pointer;">Logout</button>
            </div>
        </div>
    `;
    
    // Remove any existing modal
    const existingModal = document.getElementById('mandatory-feedback-modal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Add modal to page
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    // Prevent page interaction
    document.body.style.overflow = 'hidden';
}

function goToFeedbackPage() {
    window.location.href = '/Feedback.html';
}

function logoutUser() {
    handleLogout();
}

// Function to check and enforce feedback requirement on page load
async function enforceFeedbackRequirement() {
    // Don't enforce on login page, feedback page, or logout
    const currentPage = window.location.pathname;
    const exemptPages = ['/Login Page.html', '/Feedback.html', '/logout', '/home.html'];
    
    if (exemptPages.some(page => currentPage.includes(page))) {
        console.log('📄 Page exempt from feedback requirement:', currentPage);
        return true;
    }
    
    try {
        const accessCheck = await checkPortalAccess();
        
        if (!accessCheck.canAccess) {
            console.log('🚫 Portal access denied:', accessCheck.reason);
            
            let message = 'Please complete your monthly feedback to continue.';
            if (accessCheck.reason === 'FEEDBACK_REQUIRED_NEVER_SUBMITTED') {
                message = 'Welcome! As a new user, you must complete the monthly feedback form to access the portal.';
            } else if (accessCheck.reason === 'FEEDBACK_REQUIRED_EXPIRED') {
                const daysSince = accessCheck.daysSinceLastFeedback;
                message = `Your monthly feedback is due (last submitted ${daysSince} days ago). Please complete it to continue.`;
            }
            
            showMandatoryFeedbackModal(message);
            return false;
        }
        
        console.log('✅ Portal access granted');
        return true;
    } catch (error) {
        console.error('❌ Error enforcing feedback requirement:', error);
        // On error, allow access but log the issue
        return true;
    }
}
