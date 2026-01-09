'use client';

import { useCallback } from 'react';
import { toast } from 'sonner';

export type NotificationType = 'success' | 'error' | 'info' | 'warning';

export const useNotification = () => {
  const notify = useCallback((message: string, type: NotificationType = 'info') => {
    switch (type) {
      case 'success':
        toast.success(message);
        break;
      case 'error':
        toast.error(message);
        break;
      case 'warning':
        toast.warning(message);
        break;
      case 'info':
      default:
        toast.info(message);
        break;
    }
  }, []);

  const success = useCallback((message: string) => {
    notify(message, 'success');
  }, [notify]);

  const error = useCallback((message: string) => {
    notify(message, 'error');
  }, [notify]);

  const warning = useCallback((message: string) => {
    notify(message, 'warning');
  }, [notify]);

  const info = useCallback((message: string) => {
    notify(message, 'info');
  }, [notify]);

  return {
    notify,
    success,
    error,
    warning,
    info,
  };
};
