import {
  Injectable
} from '@angular/core';

interface JwtPayload {
  sub?: string;
  role?: string;
  iat?: number;
  exp?: number;
}

@Injectable({
  providedIn: 'root'
})
export class JwtService {

  decodeToken(
    token: string
  ): JwtPayload | null {

    try {

      const tokenParts =
        token.split('.');

      if (tokenParts.length !== 3) {
        return null;
      }

      const encodedPayload =
        tokenParts[1];

      const normalizedPayload =
        encodedPayload
          .replace(/-/g, '+')
          .replace(/_/g, '/');

      const decodedPayload =
        decodeURIComponent(
          atob(normalizedPayload)
            .split('')
            .map(character => {

              const hexadecimal =
                character
                  .charCodeAt(0)
                  .toString(16)
                  .padStart(2, '0');

              return `%${hexadecimal}`;
            })
            .join('')
        );

      return JSON.parse(
        decodedPayload
      ) as JwtPayload;

    } catch {

      return null;
    }
  }

  isTokenExpired(
    token: string
  ): boolean {

    const payload =
      this.decodeToken(token);

    if (!payload?.exp) {
      return true;
    }

    const currentTimeInSeconds =
      Math.floor(
        Date.now() / 1000
      );

    return payload.exp
      <= currentTimeInSeconds;
  }

  getSubject(
    token: string
  ): string | null {

    return this.decodeToken(token)?.sub
      ?? null;
  }

  getRole(
    token: string
  ): string | null {

    return this.decodeToken(token)?.role
      ?? null;
  }

  getExpirationDate(
    token: string
  ): Date | null {

    const expiration =
      this.decodeToken(token)?.exp;

    if (!expiration) {
      return null;
    }

    return new Date(
      expiration * 1000
    );
  }
}