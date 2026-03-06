# MyLog 디자인 프롬프트

## 인증 화면

### 로그인 화면
Create a clean login screen with an email input field (8-30 characters), a password input field (8-20 characters), a primary login button, and a sign-up link at the bottom. Use a centered card layout with a minimalist design, clear input labels, and error validation messages below each field. The login button should be prominent and the sign-up link should be subtle, based on the design system.

### 회원가입 화면
Create a user registration screen with an email input field (required, email format), a name input field (3-30 characters, required), a password input field (password rules, required), a sign-up button, and a login link at the bottom. Use a vertical form layout with clear input labels, password strength indicators, and validation messages. The sign-up button should be prominent and the login link should be secondary, based on the design system.

### 소셜 로그인 화면
Create a social login screen with a Google login button, a Kakao login button, a Naver login button, and an email login link at the bottom. Use a centered layout with each social login button having its brand colors and icons. The email login link should be subtle and positioned below the social buttons, based on the design system.

---

## 게시글 화면

### 게시글 목록 화면 (전체/내 게시글/검색 결과)
Create an article list screen with article cards displaying title, author, category, thumbnail image, tag list, and creation date. Include a search field for keywords, a tag filter section, and pagination at the bottom. Use a responsive grid layout for cards with hover effects. Each card should have a consistent visual hierarchy with the thumbnail at the top, title below, metadata at the bottom, and tags as small badges. Search and filter should be in a prominent header section, based on the design system.

### 게시글 상세 화면
Create an article detail screen with article information including title, author, category, representative image, body content, tag list, creation date, and modification date. Include a comment section with comment list and comment creation form. For the article author, show edit and delete buttons. Use a clean layout with the article content centered, metadata clearly separated, and comments section below with a nested design for replies, based on the design system.

### 게시글 작성 화면
Create an article creation screen with a title input field (5-30 characters, required), a category selector (required), a body text input field (10-3000 characters, required), a tag input for multiple selection (required), an image upload area (required), a create button, and a cancel button. Use a vertical form layout with a rich text editor for the body, a drag-and-drop image upload area with preview, and a tag selector with autocomplete. The create button should be prominent and the cancel button secondary, based on the design system.

### 게시글 수정 화면
Create an article update screen with a title input field (5-30 characters, required), a category selector (required), a body text input field (10-3000 characters, required), a tag input for multiple selection (required), an existing image display, a new image upload area (optional), an update button, and a cancel button. Use a vertical form layout similar to the creation screen but with the current article data pre-filled, the existing image shown with a remove option, and the image upload clearly marked as optional, based on the design system.

---

## 댓글 화면

### 댓글 섹션 (게시글 상세 내)
Create a comment section within an article detail with a comment creation form containing a comment content input field (5-200 characters, required) and a submit button. Below the form, display a list of comment items showing author name, comment content, creation date, and modification date. For nested replies, display a reply list with author name, reply content, and creation date. Show edit and delete buttons for comment authors, and a reply button for creating nested comments. Use a threaded layout with visual indentation for replies and clear visual separation between comments, based on the design system.

### 내 댓글 목록 화면
Create a my comments list screen with a comment list showing comment content, creation date, modification date, and a related article link. Include pagination at the bottom. Use a clean card or list layout with each comment item clearly showing the content and metadata, and the related article link prominently displayed, based on the design system.

### 내 게시글의 댓글 목록 화면
Create a comments on my articles list screen with a comment list showing author name, comment content, creation date, and related article title. Include pagination at the bottom. Use a card or list layout with clear visual separation between comments, author information prominently displayed, and the related article title as a clickable link, based on the design system.

---

## 프로필 화면

### 내 프로필 조회 화면
Create a profile view screen with a profile image, email, name, nickname, bio, OAuth provider information, registration date, last modification date, a profile update button, and an account delete button. Use a centered layout with the profile image prominently displayed at the top, information clearly organized in sections, and action buttons at the bottom with different visual styling (update as primary, delete as danger/secondary), based on the design system.

### 프로필 수정 화면
Create a profile update screen with an existing profile image display, a new profile image upload area (optional), a name input field (3-30 characters, required), a nickname input field (3-30 characters, required), a bio input field (max 200 characters, required), a password input field (required), an update button, and a cancel button. Use a vertical form layout with the existing image shown with preview, a drag-and-drop upload area, clear input labels, and the update button prominent, based on the design system.

---

## 카테고리 화면

### 카테고리 목록 화면
Create a category list screen with category items showing category name, a create category button, an edit button for each category, and a delete button for each category. Use a card or list layout with each category item having action buttons on the right or in a dropdown menu. The create button should be prominently placed at the top of the screen, based on the design system.

### 카테고리 생성 화면
Create a category creation screen with a category name input field (1-12 characters, required), a create button, and a cancel button. Use a simple centered form layout with a clear input label, validation message below the field, and the create button prominent, based on the design system.

### 카테고리 수정 화면
Create a category update screen with a category name input field (1-12 characters, required), an update button, and a cancel button. Use a simple centered form layout similar to the creation screen but with the current category name pre-filled, based on the design system.

---

## 알림 화면

### 알림 목록 화면
Create a notification list screen with notification items showing notification message, notification type, related article link, and read status (click to mark as read). Include a notification settings button and pagination at the bottom. Use a card or list layout with unread notifications visually highlighted, notification type icons for quick recognition, and the settings button in a prominent header position, based on the design system.

### 알림 설정 화면
Create a notification settings screen with a notification type list showing notification type name and a toggle switch for each notification type (on/off). Include a save button at the bottom. Use a clean list layout with each notification type clearly labeled, toggle switches easily accessible, and the save button prominently displayed at the bottom, based on the design system.
