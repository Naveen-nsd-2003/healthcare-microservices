export type NomineeRelationship =
  | 'SPOUSE'
  | 'FATHER'
  | 'MOTHER'
  | 'SON'
  | 'DAUGHTER'
  | 'BROTHER'
  | 'SISTER'
  | 'LEGAL_GUARDIAN'
  | 'OTHER';

  export interface NomineeRelationshipOption {
  value: NomineeRelationship;
  label: string;
}

export const NOMINEE_RELATIONSHIP_OPTIONS:
  NomineeRelationshipOption[] = [
    {
      value: 'SPOUSE',
      label: 'Spouse'
    },
    {
      value: 'FATHER',
      label: 'Father'
    },
    {
      value: 'MOTHER',
      label: 'Mother'
    },
    {
      value: 'SON',
      label: 'Son'
    },
    {
      value: 'DAUGHTER',
      label: 'Daughter'
    },
    {
      value: 'BROTHER',
      label: 'Brother'
    },
    {
      value: 'SISTER',
      label: 'Sister'
    },
    {
      value: 'LEGAL_GUARDIAN',
      label: 'Legal guardian'
    },
    {
      value: 'OTHER',
      label: 'Other'
    }
  ];