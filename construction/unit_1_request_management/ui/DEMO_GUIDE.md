# Access Request Management System - Demo Guide

This guide walks you through the features and workflows of the Access Request Management System UI.

## Quick Start

### Option 1: Local Development

1. **Install dependencies**:
```bash
npm install
```

2. **Start development server**:
```bash
npm run dev
```

3. **Open browser**:
Navigate to [http://localhost:3000](http://localhost:3000)

### Option 2: Docker Compose

1. **Build and start all services**:
```bash
docker-compose up -d
```

2. **Access the application**:
- UI: [http://localhost:3000](http://localhost:3000)
- Backend API: [http://localhost:8080](http://localhost:8080)
- PostgreSQL: localhost:5432
- Kafka: localhost:9092

3. **Stop services**:
```bash
docker-compose down
```

## Demo Credentials

### Employee Role
```
Email: employee@example.com
Password: password123
```

### Head of Office Role
```
Email: headofoffice@example.com
Password: password123
```

### Reviewer Role
```
Email: reviewer@example.com
Password: password123
```

### Administrator Role
```
Email: admin@example.com
Password: password123
```

## Workflow Demonstrations

### 1. Creating a New Request

**Steps**:
1. Log in with Employee credentials
2. Click "Create Request" button
3. Fill in the form:
   - **Access Type**: Select "Operating System" or "Web Application"
   - **System Name**: Enter a system name (e.g., "SAP ERP System")
   - **Justification**: Provide detailed justification (minimum 10 characters)
   - **Documents**: Optionally upload supporting documents
4. Click "Create Request"
5. You'll be redirected to the request details page

**Expected Result**: Request is created in "Draft" status

### 2. Submitting a Request for Approval

**Steps**:
1. From the request details page, click "Submit Request"
2. Select the Head of Office from the dropdown
3. Confirm the submission
4. The request status changes to "Pending Initial Approval"

**Expected Result**: Request is now awaiting Head of Office approval

### 3. Approving a Request (Head of Office)

**Steps**:
1. Log out and log in with Head of Office credentials
2. Go to "Pending Approvals" page
3. Find the request and click "Review"
4. Click "Approve" button
5. Optionally add comments
6. Confirm approval

**Expected Result**: Request status changes to "Pending Review"

### 4. Declining a Request

**Steps**:
1. From the request details page, click "Decline" button
2. Enter a decline reason (minimum 10 characters)
3. Confirm decline

**Expected Result**: Request status changes to "Declined"

### 5. Endorsing a Request (Reviewer)

**Steps**:
1. Log in with Reviewer credentials
2. Go to "Pending Approvals" page
3. Find the request and click "Review"
4. Click "Endorse" button
5. Optionally add comments
6. Confirm endorsement

**Expected Result**: Request status changes to "Pending Final Approval"

### 6. Final Approval (Head)

**Steps**:
1. Log in with Head credentials
2. Go to "Pending Approvals" page
3. Find the request and click "Review"
4. Click "Approve" button
5. Confirm approval

**Expected Result**: Request status changes to "Approved"

### 7. Implementing a Request (Administrator)

**Steps**:
1. Log in with Administrator credentials
2. Go to "Pending Approvals" page
3. Find the approved request and click "Review"
4. Click "Implement" button
5. Optionally add implementation notes
6. Confirm implementation

**Expected Result**: Request status changes to "Implemented"

## Page Descriptions

### Home Page
- Landing page with system overview
- Quick links to sign in or view demo
- Feature highlights

### Login Page
- Email and password authentication
- Demo login button for quick access
- Note about backend requirements

### Dashboard
- Overview of request statistics
- Quick action buttons
- Recent requests table
- User profile and logout

### Requests List
- View all requests with pagination
- Filter by status
- Sort by different columns
- Quick access to request details

### Request Details
- Complete request information
- Approval chain and status
- Request history timeline
- Attached documents
- Action buttons based on user role

### Create Request
- Form to create new request
- Access type selection
- System name input
- Justification textarea
- Document upload section

### Pending Approvals
- List of requests awaiting user's action
- Quick review buttons
- Request summary information

## Key Features to Explore

### 1. Request Lifecycle
- Observe how requests move through different statuses
- Track the approval chain
- View complete audit trail

### 2. Role-Based Access
- Different users see different actions
- Permissions based on user role
- Appropriate approval workflows

### 3. Request History
- Timeline of all actions
- Actor information
- Status transitions
- Comments and notes

### 4. Search and Filter
- Filter requests by status
- Search by request ID or system name
- Pagination for large result sets

### 5. Responsive Design
- Test on different screen sizes
- Mobile-friendly interface
- Tablet optimization

## Testing Scenarios

### Scenario 1: Happy Path
1. Create request as Employee
2. Submit to Head of Office
3. Approve as Head of Office
4. Endorse as Reviewer
5. Approve as Head
6. Implement as Administrator

### Scenario 2: Decline Path
1. Create request as Employee
2. Submit to Head of Office
3. Decline as Head of Office
4. Observe request status changes to "Declined"

### Scenario 3: Return Path
1. Create request as Employee
2. Submit to Head of Office
3. Approve as Head of Office
4. Endorse as Reviewer
5. Return as Head
6. Re-endorse as Reviewer
7. Approve as Head
8. Implement as Administrator

### Scenario 4: Multiple Requests
1. Create multiple requests with different access types
2. Filter by status
3. Observe pagination
4. Test search functionality

## Troubleshooting

### Issue: "Failed to load requests"
**Solution**: 
- Ensure backend API is running on `http://localhost:8080`
- Check network tab in browser developer tools
- Verify CORS is enabled on backend

### Issue: Login fails
**Solution**:
- Verify backend is running
- Check credentials are correct
- Clear browser cache and cookies
- Try demo login button

### Issue: Buttons don't work
**Solution**:
- Ensure you're logged in
- Check user role has permission for action
- Verify request is in correct status
- Check browser console for errors

### Issue: Slow performance
**Solution**:
- Check network latency
- Verify backend performance
- Clear browser cache
- Try in incognito mode

## Performance Testing

### Load Testing
1. Create multiple requests
2. List requests with pagination
3. Search across large datasets
4. Monitor response times

### Browser Compatibility
- Test in Chrome, Firefox, Safari, Edge
- Verify responsive design
- Check form validation
- Test keyboard navigation

## API Testing

### Using Browser DevTools
1. Open Network tab
2. Perform actions in UI
3. Observe API calls
4. Check request/response payloads
5. Verify status codes

### Using cURL
```bash
# Get requests
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/requests

# Create request
curl -X POST \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"accessType":"OS","systemName":"Test","justification":"Test justification"}' \
  http://localhost:8080/api/v1/requests
```

## Next Steps

### For Development
1. Review the code structure
2. Understand the component hierarchy
3. Explore the API integration
4. Modify components and test changes

### For Deployment
1. Build production bundle: `npm run build`
2. Test production build: `npm start`
3. Configure environment variables
4. Deploy to hosting platform

### For Enhancement
1. Add more pages (Reports, Settings, etc.)
2. Implement real-time notifications
3. Add advanced search filters
4. Implement data export functionality
5. Add user preferences

## Support

For issues or questions:
1. Check the README.md for detailed documentation
2. Review the code comments
3. Check browser console for errors
4. Verify backend API is running
5. Contact the development team

## Additional Resources

- [Next.js Documentation](https://nextjs.org/docs)
- [React Documentation](https://react.dev)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [TypeScript Documentation](https://www.typescriptlang.org/docs)
- [Zod Documentation](https://zod.dev)
