# Access Request Management System - UI

A modern React/Next.js user interface for managing access requests with multi-level approval workflow orchestration.

## Features

- **Request Management**: Create, view, and manage access requests
- **Multi-Level Approval**: Route requests through Head of Office, Reviewer, and Head
- **Request Tracking**: Monitor request status and view complete audit trail
- **Role-Based Access**: Different views and actions based on user role
- **Responsive Design**: Works on mobile, tablet, and desktop
- **Real-time Notifications**: Toast notifications for user actions

## Technology Stack

- **Framework**: Next.js 14+ with TypeScript
- **UI Library**: Tailwind CSS
- **State Management**: React Context API + Zustand
- **Form Handling**: React Hook Form with Zod validation
- **HTTP Client**: Axios with interceptors
- **Notifications**: Sonner
- **Date Handling**: date-fns

## Project Structure

```
ui/
├── app/                          # Next.js app directory
│   ├── layout.tsx               # Root layout
│   ├── page.tsx                 # Home page
│   ├── login/                   # Login page
│   ├── dashboard/               # Dashboard page
│   ├── requests/                # Requests pages
│   │   ├── page.tsx            # Requests list
│   │   ├── create/             # Create request
│   │   └── [id]/               # Request details
│   └── approvals/               # Approvals page
├── components/                   # Reusable UI components
├── services/                     # API service layer
│   ├── api.ts                  # API client configuration
│   ├── requestService.ts       # Request API endpoints
│   ├── accessTypeService.ts    # Access type API endpoints
│   └── authService.ts          # Authentication
├── hooks/                        # Custom React hooks
│   ├── useAuth.ts              # Authentication hook
│   ├── useRequests.ts          # Requests hook
│   └── useNotification.ts      # Notifications hook
├── types/                        # TypeScript type definitions
├── utils/                        # Utility functions
│   ├── formatters.ts           # Date and status formatters
│   ├── validators.ts           # Zod validation schemas
│   └── constants.ts            # Application constants
├── styles/                       # Global styles
├── public/                       # Static assets
├── .env.local                   # Environment variables
├── next.config.js              # Next.js configuration
├── tsconfig.json               # TypeScript configuration
├── tailwind.config.js          # Tailwind CSS configuration
└── package.json                # Dependencies
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend API running on `http://localhost:8080/api/v1`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Configure environment variables in `.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_NAME=Access Request Management System
NEXT_PUBLIC_APP_VERSION=1.0.0
```

3. Start the development server:
```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm start` - Start production server
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript type checking

## API Integration

The UI communicates with the backend API at `http://localhost:8080/api/v1`.

### Authentication

- JWT token-based authentication
- Token stored in localStorage
- Automatically included in all API requests
- Redirects to login on 401 Unauthorized

### API Endpoints

#### Requests
- `GET /requests` - List requests with pagination and filters
- `GET /requests/{id}` - Get request details
- `POST /requests` - Create new request
- `PUT /requests/{id}/submit` - Submit request for approval
- `PUT /requests/{id}/approve` - Approve request
- `PUT /requests/{id}/decline` - Decline request
- `PUT /requests/{id}/endorse` - Endorse request
- `PUT /requests/{id}/return` - Return request
- `PUT /requests/{id}/implement` - Mark as implemented
- `GET /requests/{id}/history` - Get request history
- `GET /requests/search` - Search requests

#### Access Types
- `GET /access-types` - List access types
- `GET /access-types/{id}` - Get access type details
- `POST /access-types` - Create access type
- `PUT /access-types/{id}/routing` - Update routing

## User Roles

- **Employee**: Create requests, view own requests
- **Head of Office**: Approve/decline requests from their office
- **SMD/RDC Reviewer**: Review and endorse requests
- **SMD/RDC Head**: Final approval and return requests
- **System/Database Administrator**: Implement requests
- **System Admin**: Manage access types and routing

## Demo Credentials

```
Email: employee@example.com
Password: password123
```

## Development

### Adding New Pages

1. Create a new directory in `app/`
2. Add `page.tsx` file
3. Use existing hooks and services for API calls
4. Follow the established component structure

### Adding New API Endpoints

1. Add method to appropriate service in `services/`
2. Define types in `types/index.ts`
3. Add validation schema in `utils/validators.ts`
4. Use in components via custom hooks

### Styling

- Uses Tailwind CSS for styling
- Global styles in `app/globals.css`
- Component-level styles using Tailwind classes
- Responsive design with mobile-first approach

## Error Handling

- API errors are caught and displayed as toast notifications
- Form validation errors are shown inline
- Network errors are handled gracefully
- Unauthorized requests redirect to login

## Performance Optimization

- Code splitting with dynamic imports
- Image optimization
- Lazy loading of components
- Efficient state management
- Memoization of expensive computations

## Accessibility

- ARIA labels and roles
- Keyboard navigation support
- Color contrast compliance
- Screen reader support
- Focus management

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Backend Connection Issues

If you see "Failed to load requests" errors:
1. Ensure backend API is running on `http://localhost:8080`
2. Check `NEXT_PUBLIC_API_URL` in `.env.local`
3. Verify CORS is enabled on backend
4. Check browser console for detailed error messages

### Authentication Issues

If you're redirected to login unexpectedly:
1. Check if token is stored in localStorage
2. Verify token hasn't expired
3. Try logging in again
4. Clear browser cache and cookies

### Build Issues

If you encounter build errors:
1. Run `npm install` to ensure all dependencies are installed
2. Run `npm run type-check` to check for TypeScript errors
3. Clear `.next` directory: `rm -rf .next`
4. Rebuild: `npm run build`

## Contributing

1. Follow the existing code structure and naming conventions
2. Use TypeScript for type safety
3. Add proper error handling
4. Test changes locally before committing
5. Update documentation as needed

## License

This project is part of the Access Request Processing System.

## Support

For issues or questions, please contact the development team.
