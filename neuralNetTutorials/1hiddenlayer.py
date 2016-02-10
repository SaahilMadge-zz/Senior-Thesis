import numpy as np

num_examples = len(X) #training set size
nn_input_dim = 2 #input layer dim
nn_output_dim = 2 #output layer dim

# Gradient descent parameters
epsilon = 0.01
reg_lambda = 0.01

# Helper function to plot a decision boundary.
# If you don't fully understand this function don't worry, it just generates the contour plot below.
def plot_decision_boundary(pred_func):
    # Set min and max values and give it some padding
    x_min, x_max = X[:, 0].min() - .5, X[:, 0].max() + .5
    y_min, y_max = X[:, 1].min() - .5, X[:, 1].max() + .5
    h = 0.01
    # Generate a grid of points with distance h between them
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h), np.arange(y_min, y_max, h))
    # Predict the function value for the whole gid
    Z = pred_func(np.c_[xx.ravel(), yy.ravel()])
    Z = Z.reshape(xx.shape)
    # Plot the contour and training examples
    plt.contourf(xx, yy, Z, cmap=plt.cm.Spectral)
    plt.scatter(X[:, 0], X[:, 1], c=y, cmap=plt.cm.Spectral)

# Helper function to find total loss
def calculate_loss(model):
	W1, b1, W2, b2 = model['W1'], model['b1'], model['W2'], model['b2']
	# Forward propagation
	z1 = X.dot(W1) + b1
	a1 = np.tanh(z1)
	z2 = a1.dot(W2) + b2
	exp_scores = np.exp(z2)
	# do softmax
	probs = exp_scores / np.sum(exp_scores, axis=1, keepdims=True)
	# Calculate the full loss
	correct_logprobs = -np.log(probs[range(num_examples), y])
	data_loss = np.sum(correct_logprobs)
	# Add regularization term
	data_loss += reg_lambda/2 * (np.sum(np.square(W1)) + np.sum(np.square(W2)))
	return 1./num_examples * data_loss

# Helper function to predict an output
def predict(model, x):
	W1, b1, W2, b2 = model['W1'], model['b1'], model['W2'], model['b2']
	# Forward propagation
	z1 = X.dot(W1) + b1
	a1 = np.tanh(z1)
	z2 = a1.dot(W2) + b2
	exp_scores = np.exp(z2)
	# do softmax
	probs = exp_scores / np.sum(exp_scores, axis=1, keepdims=True)
	return np.argmax(probs, axis=1)

# This function learns parameters for neural network
# - nn_hdim: Number of nodes in the hidden layer
# - num_passes: Number of passes through training data
# - print_loss: If True, print the loss every 1000 iterations
def build_model(nn_hdim, num_passes=20000, print_loss=False):
	# Initialize parameters to random values
	np.random.seed(0)
	W1 = np.random.randn(nn_input_dim, nn_hdim)
	b1 = np.zeros((1, nn_hdim))
	W2 = np.random.randn(nn_hdim, nn_output_dim)
	b2 = np.zeros((1, nn_output_dim))

	model = {}

	# Gradient descent. For each batch:
	for i in xrange(0, num_passes):
		# Forward propagation
		z1 = X.dot(W1) + b1
		a1 = np.tanh(z1)
		z2 = a1.dot(W2) + b2
		exp_scores = np.exp(z2)
		probs = exp_scores / np.sum(exp_scores, axis=1, keepdims=True)

		# Backpropagation
		delta3 = probs
		delta3[range(num_examples), y] -= 1



