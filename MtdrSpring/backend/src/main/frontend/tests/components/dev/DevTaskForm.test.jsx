import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DevTaskForm from '@/components/dev/DevTaskForm';

describe('DevTaskForm', () => {
  const defaultProps = {
    formId: 'task-form',
    formData: {
      title: '',
      description: '',
      expectedHours: '',
      priority: 'MEDIUM',
      sprintId: '',
      isBug: false,
    },
    setFormData: jest.fn(),
    openMenu: null,
    setOpenMenu: jest.fn(),
    sprintOptions: [
      {
        value: '',
        label: 'Backlog',
      },
    ],
    onSubmit: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders all main fields', () => {
    render(
      <DevTaskForm {...defaultProps} />
    );

    expect(
      screen.getByPlaceholderText(
        'Update API users'
      )
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(
        'Add details...'
      )
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText('2')
    ).toBeInTheDocument();
  });

  it('calls setFormData when title changes', async () => {
    const user = userEvent.setup();

    render(
      <DevTaskForm {...defaultProps} />
    );

    const input =
      screen.getByPlaceholderText(
        'Update API users'
      );

    await user.type(input, 'Nueva tarea');

    expect(
      defaultProps.setFormData
    ).toHaveBeenCalled();
  });

  it('switches to bug mode', async () => {
    const user = userEvent.setup();

    render(
      <DevTaskForm {...defaultProps} />
    );

    await user.click(
      screen.getByRole('button', {
        name: 'Bug',
      })
    );

    expect(
      defaultProps.setFormData
    ).toHaveBeenCalled();
  });

  it('opens priority dropdown', async () => {
    const user = userEvent.setup();

    render(
      <DevTaskForm {...defaultProps} />
    );

    await user.click(
      screen.getByText('Medium')
    );

    expect(
      defaultProps.setOpenMenu
    ).toHaveBeenCalled();
  });
});