export interface ApiErrorResponse {
    success: boolean;
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    errors: string[];
}