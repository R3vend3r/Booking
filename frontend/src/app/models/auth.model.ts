export interface AuthResponse {
  userId: string;
  login: string;
  role: string;
  token: string;
}

export interface LoginRequest {
  login: string;
  password: string;
}

export interface RegistrationRequest {
  login: string;
  password: string;
  email: string;
  fullName: string;
  phone?: string;
  birthday?: string;
}
