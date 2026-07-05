/**
 * Universal Hamburger Menu Initialization
 * This script should be included after the header is loaded to ensure
 * the hamburger menu appears in front of all content with maximum z-index.
 */

// Function to initialize hamburger menu after dynamic header loading
function initializeHamburgerMenu() {
    console.log('Initializing hamburger menu...');
    
    const hamburger = document.querySelector('.hamburger');
    const menuContainer = document.querySelector('.menu-container');
    
    if (!hamburger || !menuContainer) {
        console.error('Hamburger or menu container not found after header load!', {
            hamburger: !!hamburger,
            menuContainer: !!menuContainer
        });
        return;
    }
    
    // Remove any existing event listeners by cloning elements
    const newHamburger = hamburger.cloneNode(true);
    hamburger.parentNode.replaceChild(newHamburger, hamburger);
    
    // Add click event listener to hamburger
    newHamburger.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        console.log('Hamburger clicked!');
        toggleMenu();
    });
    
    // Add touch event listener for mobile
    newHamburger.addEventListener('touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
        console.log('Hamburger touched!');
        toggleMenu();
    });
    
    // Close menu when clicking outside (but not when scrolling inside menu)
    document.addEventListener('click', function(e) {
        if (!newHamburger.contains(e.target) && !menuContainer.contains(e.target)) {
            closeMenu();
        }
    });
    
    // Handle touch events for better mobile experience
    document.addEventListener('touchstart', function(e) {
        if (!newHamburger.contains(e.target) && !menuContainer.contains(e.target)) {
            closeMenu();
        }
    });
    
    // Improve scrolling for iOS and mobile devices
    menuContainer.style.webkitOverflowScrolling = 'touch';
    
    function toggleMenu() {
        menuContainer.classList.toggle('active');
        newHamburger.classList.toggle('active');
        
        // Prevent background scrolling when menu is open
        if (menuContainer.classList.contains('active')) {
            document.body.style.overflow = 'hidden';
            document.body.style.position = 'fixed';
            document.body.style.width = '100%';
        } else {
            document.body.style.overflow = '';
            document.body.style.position = '';
            document.body.style.width = '';
        }
        
        console.log('Menu toggled. Active:', menuContainer.classList.contains('active'));
    }
    
    function closeMenu() {
        menuContainer.classList.remove('active');
        newHamburger.classList.remove('active');
        
        // Restore background scrolling
        document.body.style.overflow = '';
        document.body.style.position = '';
        document.body.style.width = '';
        
        console.log('Menu closed');
    }
    
    console.log('Hamburger menu initialized successfully');
}

// Auto-initialize if DOM is already loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        // Small delay to ensure header is loaded
        setTimeout(initializeHamburgerMenu, 100);
    });
} else {
    // DOM already loaded, initialize immediately
    setTimeout(initializeHamburgerMenu, 100);
}

// Export for manual initialization
window.initializeHamburgerMenu = initializeHamburgerMenu;
