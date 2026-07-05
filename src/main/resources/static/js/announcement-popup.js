// Announcement Popup Module - Fixed for proper page overlay
class AnnouncementPopup {
    constructor() {
        console.log('🎯 AnnouncementPopup initialized');
        this.modalId = 'announcementPopupModal';
        this.sessionKey = 'jeewan_announcement_shown';
        this.hasShownSession = sessionStorage.getItem(this.sessionKey) === 'true';
        this.modalCreated = false;
        this.isVisible = false;
    }

    createModal() {
        if (this.modalCreated) {
            console.log('Modal already exists');
            return;
        }

        console.log('Creating announcement modal...');
        
        // Create modal HTML with inline styles (no external CSS dependencies)
        const modalHTML = `
            <div id="${this.modalId}" style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100vw;
                height: 100vh;
                z-index: 999999;
                background: rgba(0, 0, 0, 0.6);
                backdrop-filter: blur(3px);
                display: none;
                justify-content: center;
                align-items: center;
                opacity: 0;
                transition: opacity 0.3s ease;
            ">
                <div style="
                    position: relative;
                    background: #ffffff;
                    border-radius: 20px;
                    padding: 35px;
                    max-width: 600px;
                    width: 90%;
                    max-height: 85vh;
                    overflow-y: auto;
                    box-shadow: 0 30px 60px rgba(0, 0, 0, 0.5);
                    transform: scale(0.8);
                    transition: transform 0.3s ease;
                ">
                    <button onclick="window.AnnouncementPopup.closeModal()" style="
                        position: absolute;
                        top: 15px;
                        right: 20px;
                        font-size: 32px;
                        color: #999;
                        cursor: pointer;
                        background: none;
                        border: none;
                        width: 40px;
                        height: 40px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        border-radius: 50%;
                    " onmouseover="this.style.color='#e74c3c'; this.style.backgroundColor='rgba(231, 76, 60, 0.1)'" onmouseout="this.style.color='#999'; this.style.backgroundColor='transparent'">&times;</button>
                    
                    <h2 style="
                        margin: 0 0 25px 0;
                        color: #2c3e50;
                        font-size: 28px;
                        text-align: center;
                        font-weight: 700;
                        border-bottom: 3px solid #f0f0f0;
                        padding-bottom: 15px;
                    ">🔔 Latest Announcements</h2>
                    
                    <div id="announcement-popup-container" style="
                        margin-bottom: 25px;
                        max-height: 400px;
                        overflow-y: auto;
                    ">
                        <div style="text-align: center; color: #666; padding: 40px 20px; font-size: 18px;">Loading announcements...</div>
                    </div>
                    
                    <div style="
                        text-align: center;
                        border-top: 2px solid #eee;
                        padding-top: 25px;
                    ">
                        <button onclick="window.AnnouncementPopup.closeModal()" style="
                            background: linear-gradient(135deg, #3498db, #2980b9);
                            color: white;
                            border: none;
                            padding: 15px 40px;
                            border-radius: 25px;
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            box-shadow: 0 4px 15px rgba(52, 152, 219, 0.3);
                        " onmouseover="this.style.background='linear-gradient(135deg, #2980b9, #21618c)'; this.style.transform='translateY(-2px)'" onmouseout="this.style.background='linear-gradient(135deg, #3498db, #2980b9)'; this.style.transform='translateY(0)'">Close</button>
                    </div>
                </div>
            </div>
        `;
        
        // Remove existing modal if any
        const existingModal = document.getElementById(this.modalId);
        if (existingModal) existingModal.remove();
        
        // Add modal to body
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        this.modalCreated = true;
        this.bindEvents();
        console.log('Modal created successfully');
    }
    
    bindEvents() {
        const modal = document.getElementById(this.modalId);
        if (!modal) return;

        // Background click to close
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeModal();
            }
        });
        
        // ESC key to close
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.isVisible) {
                this.closeModal();
            }
        });
        
        console.log('Events bound successfully');
    }
    
    async showPopup(force = false) {
        console.log('🔔 Showing announcement popup...', { force });
        
        // Don't show if already shown in this session (unless forced)
        if (this.hasShownSession && !force) {
            console.log('❌ Already shown in this session');
            return false;
        }
        
        // Create modal if needed
        if (!this.modalCreated) {
            this.createModal();
        }
        
        try {
            const announcements = await this.fetchAnnouncements();
            const activeAnnouncements = this.filterActiveAnnouncements(announcements);
            
            console.log(`Found ${activeAnnouncements.length} active announcements`);
            
            // Show modal even with no announcements if forced
            if (activeAnnouncements.length === 0 && !force) {
                console.log('No announcements to show');
                return false;
            }
            
            this.renderAnnouncements(activeAnnouncements);
            this.displayModal();
            
            // Mark as shown in session
            if (!force) {
                sessionStorage.setItem(this.sessionKey, 'true');
                this.hasShownSession = true;
            }
            
            return true;
            
        } catch (error) {
            console.error('Error showing popup:', error);
            if (force) {
                this.showErrorMessage();
                this.displayModal();
                return true;
            }
            return false;
        }
    }
    
    async fetchAnnouncements() {
        const response = await fetch('/api/announcements');
        if (!response.ok) {
            throw new Error('Failed to fetch announcements');
        }
        return await response.json();
    }
    
    filterActiveAnnouncements(announcements) {
        const now = new Date();
        return announcements.filter(ann => {
            const startTime = new Date(ann.startDatetime);
            const endTime = new Date(ann.endDatetime);
            return startTime <= now && endTime >= now;
        });
    }
    
    renderAnnouncements(announcements) {
        const container = document.getElementById('announcement-popup-container');
        
        if (announcements.length === 0) {
            container.innerHTML = '<div style="text-align: center; color: #999; padding: 40px 20px; font-style: italic; font-size: 18px;">📭 No active announcements at this time.</div>';
            return;
        }
        
        // Sort announcements by multiple criteria for better ordering
        const sortedAnnouncements = [...announcements].sort((a, b) => {
            // Primary sort: by start date (latest first)
            const dateA = new Date(a.startDatetime || '1970-01-01');
            const dateB = new Date(b.startDatetime || '1970-01-01');
            const dateDiff = dateB.getTime() - dateA.getTime();
            
            if (dateDiff !== 0) {
                return dateDiff;
            }
            
            // Secondary sort: by ID (higher ID = more recent, assuming auto-increment)
            if (a.id && b.id) {
                return b.id - a.id;
            }
            
            // Tertiary sort: by title (for consistent ordering)
            return (a.title || '').localeCompare(b.title || '');
        });
        
        console.log('Original announcements:', announcements.map(a => ({ title: a.title, startDatetime: a.startDatetime, id: a.id })));
        console.log('Sorted announcements:', sortedAnnouncements.map(a => ({ title: a.title, startDatetime: a.startDatetime, id: a.id })));
        
        const announcementsHTML = sortedAnnouncements.map(ann => `
            <div style="
                margin-bottom: 20px;
                padding: 20px;
                border-left: 6px solid #3498db;
                background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                transition: transform 0.3s ease;
            " onmouseover="this.style.transform='translateX(8px)'" onmouseout="this.style.transform='translateX(0)'">
                <h4 style="
                    margin: 0 0 12px 0;
                    color: #2c3e50;
                    font-size: 20px;
                    font-weight: 600;
                ">${this.escapeHtml(ann.title)}</h4>
                <p style="
                    margin: 0;
                    color: #555;
                    line-height: 1.7;
                    font-size: 16px;
                ">${this.escapeHtml(ann.content)}</p>
            </div>
        `).join('');
        
        container.innerHTML = announcementsHTML;
    }
    
    showErrorMessage() {
        const container = document.getElementById('announcement-popup-container');
        container.innerHTML = '<div style="text-align: center; color: #999; padding: 40px 20px; font-style: italic;">⚠️ Unable to load announcements. Please try again later.</div>';
    }
    
    displayModal() {
        const modal = document.getElementById(this.modalId);
        const content = modal.querySelector('div');
        
        if (!modal) {
            console.error('Modal not found');
            return;
        }
        
        console.log('Displaying modal...');
        
        // Ensure page is visible first
        document.body.style.visibility = 'visible';
        document.body.style.opacity = '1';
        
        // Show modal
        modal.style.display = 'flex';
        this.isVisible = true;
        
        // Animate in
        setTimeout(() => {
            modal.style.opacity = '1';
            content.style.transform = 'scale(1)';
        }, 10);
        
        // Prevent body scroll but keep page visible
        const originalOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        
        // Store original overflow to restore later
        modal.setAttribute('data-original-overflow', originalOverflow || '');
        
        console.log('Modal displayed');
    }
    
    closeModal() {
        const modal = document.getElementById(this.modalId);
        const content = modal.querySelector('div');
        
        if (!modal) return;
        
        console.log('Closing modal...');
        
        // Animate out
        modal.style.opacity = '0';
        content.style.transform = 'scale(0.8)';
        
        this.isVisible = false;
        
        // Hide after animation
        setTimeout(() => {
            modal.style.display = 'none';
            // Restore original body overflow
            const originalOverflow = modal.getAttribute('data-original-overflow') || '';
            document.body.style.overflow = originalOverflow;
        }, 300);
        
        console.log('Modal closed');
    }
    
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Create global instance
window.AnnouncementPopup = new AnnouncementPopup();

// Auto-show logic - Show immediately regardless of page state
// document.addEventListener('DOMContentLoaded', () => {
//     console.log('🚀 Announcement popup loaded');
//     
//     const currentPage = decodeURIComponent(window.location.pathname);
//     const autoShowPages = ['/home.html', '/User Profile.html', '/', '/index.html'];
//     
//     console.log('Decoded current page:', currentPage);
//     
//     const shouldAutoShow = autoShowPages.some(page => 
//         currentPage.endsWith(page) || currentPage === page ||
//         currentPage.includes('home.html') || currentPage.includes('User Profile.html')
//     );
//     
//     if (shouldAutoShow) {
//         console.log('✅ Auto-showing popup immediately for:', currentPage);
//         
//         // Show popup immediately without waiting for page load
//         setTimeout(() => {
//             console.log('🎯 Showing popup immediately...');
//             window.AnnouncementPopup.showPopup().then(shown => {
//                 console.log(shown ? '✅ Popup shown' : '❌ No announcements');
//             }).catch(error => {
//                 console.error('Error:', error);
//             });
//         }, 500); // Minimal delay just to ensure DOM is ready
//     } else {
//         console.log('❌ Not showing popup for:', currentPage);
//     }
// });

// Test function
window.testAnnouncementPopup = function() {
    console.log('🧪 Testing popup...');
    window.AnnouncementPopup.showPopup(true);
};

// Function to reset session and test fresh
window.resetAndTestPopup = function() {
    console.log('🔄 Resetting session and testing...');
    sessionStorage.removeItem('jeewan_announcement_shown');
    window.AnnouncementPopup.hasShownSession = false;
    
    // Show popup immediately
    window.AnnouncementPopup.showPopup(true);
};

// Global function to show announcement popup (called from header button)
window.showAnnouncementPopup = function() {
    console.log('🔔 Manual announcement popup trigger from header');
    
    if (window.AnnouncementPopup) {
        // Force show popup regardless of session
        window.AnnouncementPopup.showPopup(true).then(shown => {
            if (shown) {
                console.log('✅ Popup shown successfully');
            } else {
                console.log('❌ Failed to show popup');
                // Fallback: redirect to announcements page if popup fails
                window.location.href = '/announcements.html';
            }
        }).catch(error => {
            console.error('Error showing popup:', error);
            // Fallback: redirect to announcements page if popup fails
            window.location.href = '/announcements.html';
        });
    } else {
        console.log('❌ AnnouncementPopup not available, redirecting to page');
        // Fallback: redirect to announcements page if script not loaded
        window.location.href = '/announcements.html';
    }
};
