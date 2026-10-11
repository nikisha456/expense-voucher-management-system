import { z } from 'zod';
import { todayInputValue } from '../utils/format';

export const voucherSchema = z.object({
  voucherDate: z.string().min(1, 'Voucher date is required'),
  expenseDate: z.string().min(1, 'Expense date is required'),
  departmentName: z.string().min(1, 'Department is required'),
  expenseTitle: z.string().trim().min(1, 'Expense title is required').max(200, 'Expense title must be at most 200 characters'),
  expenseCategory: z.string().min(1, 'Expense category is required'),
  expenseDescription: z.string().max(2000, 'Description must be at most 2000 characters').optional().or(z.literal('')),
  amount: z.string().trim().min(1, 'Amount is required').refine((value) => /^\d+(\.\d{1,2})?$/.test(value) && Number(value) > 0, {
    message: 'Amount must be greater than 0 with at most 2 decimal places',
  }),
}).superRefine((value, ctx) => {
  if (value.expenseDate && value.expenseDate > todayInputValue()) {
    ctx.addIssue({ code: 'custom', path: ['expenseDate'], message: 'Expense date cannot be in the future' });
  }
});

export const registerSchema = z.object({
  name: z.string().trim().min(2, 'Name must be between 2 and 100 characters').max(100, 'Name must be between 2 and 100 characters'),
  email: z.string().trim().min(1, 'Email is required').email('Enter a valid email address'),
  employeeId: z.string().trim().max(50, 'Employee ID must be at most 50 characters').optional().or(z.literal('')),
  department: z.string().min(1, 'Department is required'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a number')
    .regex(/[a-z]/, 'Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a number')
    .regex(/[A-Z]/, 'Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a number')
    .regex(/\d/, 'Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a number'),
  confirmPassword: z.string().min(1, 'Confirm your password'),
}).refine((value) => value.password === value.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

export const rejectSchema = z.object({
  reason: z.string().trim().min(10, 'Rejection reason must be at least 10 characters').max(1000, 'Rejection reason must be at most 1000 characters'),
});
